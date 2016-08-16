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

package org.wildfly.extension.nosql.driver;

import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.nosql.subsystem.orientdb.OrientSubsystemService;
import org.wildfly.nosql.common.NoSQLLogger;
import org.wildfly.nosql.common.spi.NoSQLConnection;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OrientClientConnectionsService implements Service<OrientClientConnectionsService>, NoSQLConnection {

    private Configuration configuration;

    private final OrientInteraction orientInteraction;

    private final InjectedValue<OrientSubsystemService> orientSubsystemServiceInjectedValue = new InjectedValue<>();

    private final InjectedValue<OutboundSocketBinding> outboundSocketBindingInjectedValue = new InjectedValue<>();

    public OrientClientConnectionsService(Configuration configuration, OrientInteraction orientInteraction) {
        this.configuration = configuration;
        this.orientInteraction = orientInteraction;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        initOrientSubsystemService();
        initDatabaseUrl();
    }

    @Override
    public void stop(StopContext stopContext) {
        orientSubsystemServiceInjectedValue.getValue().removeModuleNameFromJndi(configuration.getJndiName());
        orientSubsystemServiceInjectedValue.getValue().removeModuleNameFromProfile(configuration.getProfileName());
    }

    @Override
    public OrientClientConnectionsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (orientInteraction.getDatabasePoolClass().isAssignableFrom(clazz)) {
            return (T) orientInteraction.getDatabasePool();
        }
        throw NoSQLLogger.ROOT_LOGGER.unassignable(clazz);
    }

    public InjectedValue<OrientSubsystemService> getOrientSubsystemServiceInjectedValue() {
        return orientSubsystemServiceInjectedValue;
    }

    public InjectedValue<OutboundSocketBinding> getOutboundSocketBindingInjectedValue() {
        return outboundSocketBindingInjectedValue;
    }

    private void initOrientSubsystemService() {
        orientSubsystemServiceInjectedValue.getValue().addModuleNameFromJndi(configuration.getJndiName(),
                configuration.getModuleName());
        orientSubsystemServiceInjectedValue.getValue().addModuleNameFromProfile(configuration.getProfileName(),
                configuration.getModuleName());
    }

    private void initDatabaseUrl() {
        Configuration.Builder configurationBuilder = new Configuration.Builder(configuration);
        configurationBuilder.databaseUrl(getDatabaseUrl(outboundSocketBindingInjectedValue.getValue(), configuration));
        // TODO: Eliminate the extra Configuration/ConfigurationBuilder instances
        Configuration extraConfiguration = configurationBuilder.build();
        configuration.setDatabaseUrl(extraConfiguration.getDatabaseUrl());
    }

    private String getDatabaseUrl(OutboundSocketBinding target, Configuration configuration) {
        if (target == null || target.getUnresolvedDestinationAddress() == null) {
            return configuration.getDatabase();
        }

        String databaseUrl = "remote:" + target.getUnresolvedDestinationAddress();
        if (target.getDestinationPort() > 0) {
            databaseUrl += ":" + target.getDestinationPort();
        }
        return databaseUrl + "/" + configuration.getDatabase();
    }

}
