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

package org.wildfly.extension.nosql.cdi;

import org.jboss.as.server.CurrentServiceContainer;
import org.wildfly.extension.nosql.subsystem.orientdb.OrientSubsystemService;
import org.wildfly.nosql.common.SubsystemService;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OrientExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(OrientExtension.class.getName());

    private final Class oPartitionedDatabasePoolClass;

    public OrientExtension(Class oPartitionedDatabasePoolClass) {
        this.oPartitionedDatabasePoolClass = oPartitionedDatabasePoolClass;
    }

    public void registerNoSQLSourceBeans(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        if (beanManager.getBeans(oPartitionedDatabasePoolClass, DefaultLiteral.INSTANCE).isEmpty()) {
            for(String profile: getService().profileNames()) {
                LOGGER.log(Level.INFO, "Registering " + oPartitionedDatabasePoolClass + " bean for profile {0}", profile);
                afterBeanDiscovery.addBean(getBean(beanManager, oPartitionedDatabasePoolClass, profile));
            }
        } else {
            LOGGER.log(Level.INFO, "Application contains a default " + oPartitionedDatabasePoolClass
                    + " Bean, automatic registration will be disabled");
        }
    }

    private SubsystemService getService() {
        return (SubsystemService) CurrentServiceContainer.getServiceContainer()
                .getService(OrientSubsystemService.SERVICE_NAME).getValue();
    }

    private <T> Bean<T> getBean(BeanManager beanManager, Class<T> beanClass, String profile) {
        BeanAttributes<T> beanAttributes = beanManager.createBeanAttributes(beanManager.createAnnotatedType(beanClass));

        return beanManager.createBean(new OrientBeanAttributes<>(beanAttributes, profile), beanClass,
                new OrientProducerFactory<>(beanClass, profile));
    }

}
