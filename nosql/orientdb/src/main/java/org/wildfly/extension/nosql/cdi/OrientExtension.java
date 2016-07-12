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

package org.wildfly.extension.nosql.cdi;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
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

    public void registerNoSQLSourceBeans(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        if (beanManager.getBeans(OPartitionedDatabasePool.class, DefaultLiteral.INSTANCE).isEmpty()) {
            for(String profile: getService().profileNames()) {
                LOGGER.log(Level.INFO, "Registering " + OPartitionedDatabasePool.class + " bean for profile {0}", profile);
                afterBeanDiscovery.addBean(getBean(beanManager, OPartitionedDatabasePool.class, profile));
            }
        } else {
            LOGGER.log(Level.INFO, "Application contains a default " + OPartitionedDatabasePool.class
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
