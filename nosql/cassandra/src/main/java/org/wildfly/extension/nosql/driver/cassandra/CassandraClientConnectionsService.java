/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.nosql.driver.cassandra;

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.util.HashMap;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.inject.MapInjector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.nosql.subsystem.cassandra.CassandraSubsystemService;
import org.wildfly.nosql.common.spi.NoSQLConnection;

/**
 * CassandraDriverService represents the connection into Cassandra
 *
 * @author Scott Marlow
 */
public class CassandraClientConnectionsService implements Service<CassandraClientConnectionsService>, NoSQLConnection {

    private final ConfigurationBuilder configurationBuilder;
    // standard application server way to obtain target hostname + port for target NoSQL database server(s)
    private Map<String, OutboundSocketBinding> outboundSocketBindings = new HashMap<String, OutboundSocketBinding>();
    private final CassandraInteraction cassandraInteraction;
    private Cluster cluster;  // represents connection into Cassandra
    private Session session;  // only set if keyspaceName is specified
    private final InjectedValue<CassandraSubsystemService> cassandraSubsystemServiceInjectedValue = new InjectedValue<>();

    public CassandraClientConnectionsService(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
        cassandraInteraction = new CassandraInteraction();
    }

    public Injector<OutboundSocketBinding> getOutboundSocketBindingInjector(String name) {
        return new MapInjector<String, OutboundSocketBinding>(outboundSocketBindings, name);
    }

    public InjectedValue<CassandraSubsystemService> getCassandraSubsystemServiceInjectedValue() {
        return cassandraSubsystemServiceInjectedValue;
    }

    @Override
    public void start(StartContext startContext) throws StartException {


        // maintain a mapping from JNDI name to NoSQL module name, that we will use during deployment time to
        // identify the static module name to add to the deployment.
        cassandraSubsystemServiceInjectedValue.getValue().addModuleNameFromJndi(configurationBuilder.getJNDIName(), configurationBuilder.getModuleName());
        cassandraSubsystemServiceInjectedValue.getValue().addModuleNameFromProfile(configurationBuilder.getDescription(), configurationBuilder.getModuleName());

        for (OutboundSocketBinding target : outboundSocketBindings.values()) {
            if (target.getDestinationPort() > 0) {
                cassandraInteraction.withPort(target.getDestinationPort());
            }
            if (target.getUnresolvedDestinationAddress() != null) {
                cassandraInteraction.addContactPoint(target.getUnresolvedDestinationAddress());
            }
        }

        if (configurationBuilder.getDescription() != null) {
            cassandraInteraction.withClusterName(configurationBuilder.getDescription());
        }
        cluster = cassandraInteraction.build();

        String keySpace = configurationBuilder.getKeySpace();
        if (keySpace != null) {
            session = cassandraInteraction.connect(cluster, keySpace);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            cassandraSubsystemServiceInjectedValue.getValue().removeModuleNameFromJndi(configurationBuilder.getJNDIName());
            cassandraSubsystemServiceInjectedValue.getValue().removeModuleNameFromProfile(configurationBuilder.getDescription());
            if (session != null) {
                cassandraInteraction.sessionClose(session);
                session = null;
            }
            cassandraInteraction.clusterClose(cluster);
            cluster = null;
        } catch (Throwable throwable) {
            ROOT_LOGGER.driverFailedToStop(throwable);
        }
    }

    @Override
    public CassandraClientConnectionsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if ( Cluster.class.isAssignableFrom( clazz ) ) {
            return (T) cluster;
        }
        if ( Session.class.isAssignableFrom( clazz)) {
            return (T) session;
        }
        throw ROOT_LOGGER.unassignable(clazz);
    }


}
