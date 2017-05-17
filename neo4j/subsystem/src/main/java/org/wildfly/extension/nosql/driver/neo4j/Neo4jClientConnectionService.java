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

package org.wildfly.extension.nosql.driver.neo4j;

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.inject.MapInjector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.security.SubjectFactory;
import org.wildfly.extension.nosql.driver.neo4j.transaction.DriverProxy;
import org.wildfly.extension.nosql.driver.neo4j.transaction.TransactionEnlistmentType;
import org.wildfly.extension.nosql.subsystem.neo4j.Neo4jSubsystemService;
import org.wildfly.nosql.common.spi.NoSQLConnection;

/**
 * Neo4jClientConnectionService represents the connection into Neo4J
 *
 * @author Scott Marlow
 */
public class Neo4jClientConnectionService implements Service<Neo4jClientConnectionService>, NoSQLConnection {

    private final ConfigurationBuilder configurationBuilder;
    // standard application server way to obtain target hostname + port for target NoSQL database server(s)
    private Map<String, OutboundSocketBinding> outboundSocketBindings = new HashMap<String, OutboundSocketBinding>();
    private final Neo4jInteraction neo4jInteraction;
    private Object /* Driver */ driver;  // Driver is thread safe but Session is not
    private final InjectedValue<Neo4jSubsystemService> neo4jSubsystemServiceInjectedValue = new InjectedValue<>();
    private final InjectedValue<SubjectFactory> subjectFactory = new InjectedValue<>();

    public InjectedValue<SubjectFactory> getSubjectFactoryInjector() {
        return subjectFactory;
    }

    public InjectedValue<Neo4jSubsystemService> getNeo4jSubsystemServiceInjectedValue() {
        return neo4jSubsystemServiceInjectedValue;
    }

    public Neo4jClientConnectionService(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
        neo4jInteraction = new Neo4jInteraction(configurationBuilder);
    }

    public Injector<OutboundSocketBinding> getOutboundSocketBindingInjector(String name) {
        return new MapInjector<String, OutboundSocketBinding>(outboundSocketBindings, name);
    }

    @Override
    public void start(StartContext startContext) throws StartException {

        // maintain a mapping from JNDI name to NoSQL module name, that we will use during deployment time to
        // identify the static module name to add to the deployment.
        neo4jSubsystemServiceInjectedValue.getValue().addModuleNameFromJndi(configurationBuilder.getJNDIName(), configurationBuilder.getModuleName());
        neo4jSubsystemServiceInjectedValue.getValue().addModuleNameFromProfile(configurationBuilder.getDescription(), configurationBuilder.getModuleName());
        for (OutboundSocketBinding target : outboundSocketBindings.values()) {
            if (target.getUnresolvedDestinationAddress() != null) {
                neo4jInteraction.addContactPoint(target.getUnresolvedDestinationAddress());
            }
            if (target.getDestinationPort() > 0) {
                neo4jInteraction.withPort(target.getDestinationPort());
            }
            if (subjectFactory.getOptionalValue() != null) {
                neo4jInteraction.subjectFactory(subjectFactory.getOptionalValue());
            }

        }

        //if (configurationBuilder.getDescription() != null) {
            // neo4jInteraction.withClusterName(configurationBuilder.getDescription());
        // }
        try {
            driver = neo4jInteraction.build();
        } catch (Throwable throwable) {
            throw new RuntimeException("could not setup ServerAddress for " + configurationBuilder.getDescription(), throwable);
        }

        if (TransactionEnlistmentType.ONEPHASECOMMIT.equals(configurationBuilder.getTransactionEnlistment())) {
            driver = onePhaseCommitWrapper(
                driver,
                neo4jSubsystemServiceInjectedValue.getValue().transactionManager(),
                neo4jSubsystemServiceInjectedValue.getValue().transactionSynchronizationRegistry());
        }

    }

    private Object /* Driver */ onePhaseCommitWrapper(Object driver, TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
            return Proxy.newProxyInstance(
                    neo4jInteraction.getDriverClass().getClassLoader(),
                    new Class[] { neo4jInteraction.getDriverClass()},
                    new DriverProxy(driver, transactionManager, transactionSynchronizationRegistry, configurationBuilder.getDescription(), configurationBuilder.getJNDIName()));
    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            neo4jSubsystemServiceInjectedValue.getValue().removeModuleNameFromJndi(configurationBuilder.getJNDIName());
            neo4jSubsystemServiceInjectedValue.getValue().removeModuleNameFromProfile(configurationBuilder.getDescription());

            neo4jInteraction.driverClose(driver);
            driver = null;
        } catch (Throwable throwable) {
            ROOT_LOGGER.driverFailedToStop(throwable);
        }
    }

    @Override
    public Neo4jClientConnectionService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public Object getDriver() {
        return driver;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if ( neo4jInteraction.getDriverClass().isAssignableFrom( clazz ) ) {
            return (T) driver;
        }
        //if ( Session.class.isAssignableFrom( clazz)) {
        //    return (T) session;
        //}
        throw ROOT_LOGGER.unassignable(clazz);
    }

}
