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

package org.wildfly.extension.nosql.driver;

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.jboss.security.SubjectFactory;
import org.wildfly.extension.nosql.subsystem.orientdb.OrientSubsystemService;
import org.wildfly.nosql.common.spi.NoSQLConnection;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OrientClientConnectionsService implements Service<OrientClientConnectionsService>, NoSQLConnection {

    private Configuration configuration;

    private final OrientInteraction orientInteraction;

    private final InjectedValue<OrientSubsystemService> orientSubsystemServiceInjectedValue = new InjectedValue<>();

    private final InjectedValue<OutboundSocketBinding> outboundSocketBindingInjectedValue = new InjectedValue<>();

    private final InjectedValue<SubjectFactory> subjectFactory = new InjectedValue<>();

    private volatile Object databasePool;

    public OrientClientConnectionsService(Configuration configuration, OrientInteraction orientInteraction) {
        this.configuration = configuration;
        this.orientInteraction = orientInteraction;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        initOrientSubsystemService();
        initDatabaseUrl();
        databasePool = orientInteraction.getDatabasePool();
    }

    @Override
    public void stop(StopContext stopContext) {
        orientSubsystemServiceInjectedValue.getValue().removeModuleNameFromJndi(configuration.getJndiName());
        orientSubsystemServiceInjectedValue.getValue().removeModuleNameFromProfile(configuration.getProfileName());
        try {
            orientInteraction.close(databasePool);
        } catch (Throwable throwable) {
            ROOT_LOGGER.driverFailedToStop(throwable);
        } finally {
            databasePool = null;
        }
    }

    @Override
    public OrientClientConnectionsService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (orientInteraction.getDatabasePoolClass().isAssignableFrom(clazz)) {
            return (T) databasePool;
        }
        throw ROOT_LOGGER.unassignable(clazz);
    }

    public InjectedValue<OrientSubsystemService> getOrientSubsystemServiceInjectedValue() {
        return orientSubsystemServiceInjectedValue;
    }

    public InjectedValue<OutboundSocketBinding> getOutboundSocketBindingInjectedValue() {
        return outboundSocketBindingInjectedValue;
    }

    public InjectedValue<SubjectFactory> getSubjectFactoryInjector() {
            return subjectFactory;
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
        if (subjectFactory.getOptionalValue() != null) {
            orientInteraction.subjectFactory(subjectFactory.getOptionalValue());
        }

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
