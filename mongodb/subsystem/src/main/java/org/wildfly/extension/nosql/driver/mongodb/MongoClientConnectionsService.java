/*
 * *
 *  * Copyright 2017 Red Hat, Inc, and individual contributors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wildfly.extension.nosql.driver.mongodb;

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.inject.MapInjector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.security.SubjectFactory;
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
    private Object /* MongoClient */ client;
    private Object /* MongoDatabase */ database;
    private MongoInteraction mongoInteraction;
    private final InjectedValue<SubjectFactory> subjectFactory = new InjectedValue<>();

    public InjectedValue<SubjectFactory> getSubjectFactoryInjector() {
        return subjectFactory;
    }

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
            try {
                mongoInteraction.hostPort(target.getUnresolvedDestinationAddress(), target.getDestinationPort());
            } catch (Throwable throwable) {
                throw new RuntimeException("could not setup ServerAddress for " + target.getUnresolvedDestinationAddress() + " " + target.getDestinationPort(),throwable);
            }
        }
        if (subjectFactory.getOptionalValue() != null) {
            mongoInteraction.subjectFactory(subjectFactory.getOptionalValue());
        }
        try {
            client = mongoInteraction.mongoClient();
        } catch (Throwable throwable) {
            throw new RuntimeException("could not setup connection to " + configurationBuilder.getDescription(),throwable);
        }

        if (configurationBuilder.getDatabase() != null) {
            try {
                database = mongoInteraction.getDB();
            } catch (Throwable throwable) {
                throw new RuntimeException("could not use database " + configurationBuilder.getDatabase(),throwable);
            }
        }

    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            mongoSubsystemServiceInjectedValue.getValue().removeModuleNameFromJndi(configurationBuilder.getJNDIName());
            mongoSubsystemServiceInjectedValue.getValue().removeModuleNameFromProfile(configurationBuilder.getDescription());
            mongoInteraction.close();
        } catch (Throwable throwable) {
            ROOT_LOGGER.driverFailedToStop(throwable);
        }
        client = null;
    }

    @Override
    public MongoClientConnectionsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public Object /* MongoClient */ getClient() {
        return client;
    }

    public Object /* MongoDatabase */ getDatabase() {
        return database;
    }

    private Class getMongoClientClass() {
        return mongoInteraction.getMongoClientClass();
    }

    private Class getMongoDatabaseClass() {
        return mongoInteraction.getMongoDatabaseClass();
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {

        if ( getMongoClientClass().isAssignableFrom( clazz ) ) {
            return (T) client;
        }
        if ( database != null && getMongoDatabaseClass().isAssignableFrom( clazz)) {
            return (T) database;
        }
        throw ROOT_LOGGER.unassignable(clazz);
    }
}
