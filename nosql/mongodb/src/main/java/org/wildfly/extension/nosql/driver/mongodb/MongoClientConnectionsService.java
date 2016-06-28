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

package org.wildfly.extension.nosql.driver.mongodb;

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.inject.MapInjector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.nosql.subsystem.mongodb.MongoSubsystemService;
import org.wildfly.nosql.common.spi.NoSQLConnection;

/**
 * MongoDriverService represents the connections into a MongoDB server
 *
 * @author Scott Marlow
 */
public class MongoClientConnectionsService implements Service<MongoClientConnectionsService>, NoSQLConnection {
    final ConfigurationBuilder configurationBuilder;
    // standard application server way to obtain target hostname + port for target NoSQL database server(s)
    private Map<String, OutboundSocketBinding> outboundSocketBindings = new HashMap<String, OutboundSocketBinding>();
    private MongoClient client;
    private MongoDatabase database;
    private MongoInteraction mongoInteraction;

    public InjectedValue<MongoSubsystemService> getMongoSubsystemServiceInjectedValue() {
        return mongoSubsystemServiceInjectedValue;
    }

    private final InjectedValue<MongoSubsystemService> mongoSubsystemServiceInjectedValue = new InjectedValue<>();

    public MongoClientConnectionsService(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
        mongoInteraction = new MongoInteraction(configurationBuilder);
    }

    public Injector<OutboundSocketBinding> getOutboundSocketBindingInjector(String name) {
        return new MapInjector<String, OutboundSocketBinding>(outboundSocketBindings, name);
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        // maintain a mapping from JNDI name to NoSQL module name, that we will use during deployment time to
        // identify the static module name to add to the deployment.
        mongoSubsystemServiceInjectedValue.getValue().addModuleNameFromJndi(configurationBuilder.getJNDIName(), configurationBuilder.getModuleName());
        mongoSubsystemServiceInjectedValue.getValue().addModuleNameFromProfile(configurationBuilder.getDescription(), configurationBuilder.getModuleName());
        for (OutboundSocketBinding target : outboundSocketBindings.values()) {
            mongoInteraction.hostPort(target.getUnresolvedDestinationAddress(), target.getDestinationPort());
        }
        client = mongoInteraction.mongoClient();
        if (configurationBuilder.getDatabase() != null) {
            database = mongoInteraction.getDB(client);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            mongoSubsystemServiceInjectedValue.getValue().removeModuleNameFromJndi(configurationBuilder.getJNDIName());
            mongoSubsystemServiceInjectedValue.getValue().removeModuleNameFromProfile(configurationBuilder.getDescription());
            mongoInteraction.close(client);
        } catch (Throwable throwable) {
            ROOT_LOGGER.driverFailedToStop(throwable);
        }
        client = null;
    }

    @Override
    public MongoClientConnectionsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if ( MongoClient.class.isAssignableFrom( clazz ) ) {
            return (T) client;
        }
        if ( MongoDatabase.class.isAssignableFrom( clazz)) {
            return (T) database;
        }
        throw ROOT_LOGGER.unassignable(clazz);
    }
}
