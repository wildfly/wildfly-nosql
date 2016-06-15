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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.jboss.as.server.CurrentServiceContainer;
import org.wildfly.extension.nosql.subsystem.mongodb.MongoSubsystemService;
import org.wildfly.nosql.common.ConnectionServiceAccess;
import org.wildfly.nosql.common.SubsystemService;
import org.wildfly.nosql.common.spi.NoSQLConnection;


/**
 * This CDI Extension registers a <code>Mongoclient</code>
 * defined by @Inject in application beans
 * Registration will be aborted if user defines her own <code>MongoClient</code> bean or producer
 *
 * TODO: eliminate dependency on MongoDB client classes so different MongoDB driver modules can be used.
 *
 * @author Antoine Sabot-Durand
 * @author Scott Marlow
 */
public class MongoExtension implements Extension {

    private static final Logger log = Logger.getLogger(MongoExtension.class.getName());

    void registerNoSQLSourceBeans(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        if (bm.getBeans(MongoClient.class, DefaultLiteral.INSTANCE).isEmpty()) {
            // Iterate profiles and create Cluster/Session bean for each profile, that application code can @Inject
            for(String profile: getService().profileNames()) {
                log.log(Level.INFO, "Registering bean for profile {0}", profile);
                abd.addBean(bm.createBean(
                        new MongoClientBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType(MongoClient.class)), profile),
                        MongoClient.class, new MongoClientProducerFactory(profile)));
                abd.addBean(bm.createBean(
                        new MongoDatabaseBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType(MongoDatabase.class)), profile),
                        MongoDatabase.class, new MongoDatabaseProducerFactory(profile)));
            }
         } else {
            log.log(Level.INFO, "Application contains a default MongoClient Bean, automatic registration will be disabled");
        }
    }

    private SubsystemService getService() {
        return (SubsystemService) CurrentServiceContainer.getServiceContainer().getService(MongoSubsystemService.serviceName()).getValue();
    }

    private static class MongoClientBeanAttributes implements BeanAttributes<MongoClient> {

        private BeanAttributes<MongoClient> delegate;
        private final String profile;

        MongoClientBeanAttributes(BeanAttributes<MongoClient> beanAttributes, String profile) {
            delegate = beanAttributes;
            this.profile = profile;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> qualifiers = new HashSet<>(delegate.getQualifiers());
            NamedLiteral namedLiteral = new NamedLiteral(profile);  // name the bean for @Inject @Named lookup
            qualifiers.add(namedLiteral);
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return ApplicationScoped.class;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return delegate.getStereotypes();
        }

        @Override
        public Set<Type> getTypes() {
            return delegate.getTypes();
        }

        @Override
        public boolean isAlternative() {
            return delegate.isAlternative();
        }
    }

    private static class MongoClientProducerFactory
            implements InjectionTargetFactory<MongoClient> {
        final String profile;

        MongoClientProducerFactory(String profile) {
            this.profile = profile;
        }

        @Override
        public InjectionTarget<MongoClient> createInjectionTarget(Bean<MongoClient> bean) {
            return new InjectionTarget<MongoClient>() {
                @Override
                public void inject(MongoClient instance, CreationalContext<MongoClient> ctx) {
                }

                @Override
                public void postConstruct(MongoClient instance) {
                }

                @Override
                public void preDestroy(MongoClient instance) {
                }

                @Override
                public MongoClient produce(CreationalContext<MongoClient> ctx) {
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection(profile);
                    return noSQLConnection.unwrap(MongoClient.class);
                }

                @Override
                public void dispose(MongoClient connection) {
                    // connection.close();
                }

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    return Collections.EMPTY_SET;
                }
            };
        }
    }

    private static class MongoDatabaseBeanAttributes implements BeanAttributes<MongoDatabase> {

        private BeanAttributes<MongoDatabase> delegate;
        private final String profile;

        MongoDatabaseBeanAttributes(BeanAttributes<MongoDatabase> beanAttributes, String profile) {
            delegate = beanAttributes;
            this.profile = profile;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            Set<Annotation> qualifiers = new HashSet<>(delegate.getQualifiers());
            NamedLiteral namedLiteral = new NamedLiteral(profile);  // name the bean for @Inject @Named lookup
            qualifiers.add(namedLiteral);
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return ApplicationScoped.class;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return delegate.getStereotypes();
        }

        @Override
        public Set<Type> getTypes() {
            return delegate.getTypes();
        }

        @Override
        public boolean isAlternative() {
            return delegate.isAlternative();
        }
    }

    private static class MongoDatabaseProducerFactory
            implements InjectionTargetFactory<MongoDatabase> {
        private final String profile;

        MongoDatabaseProducerFactory(String profile) {
            this.profile = profile;
        }

        @Override
        public InjectionTarget<MongoDatabase> createInjectionTarget(Bean<MongoDatabase> bean) {
            return new InjectionTarget<MongoDatabase>() {
                @Override
                public void inject(MongoDatabase instance, CreationalContext<MongoDatabase> ctx) {
                }

                @Override
                public void postConstruct(MongoDatabase instance) {
                }

                @Override
                public void preDestroy(MongoDatabase instance) {
                }

                @Override
                public MongoDatabase produce(CreationalContext<MongoDatabase> ctx) {
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection(profile);
                    return noSQLConnection.unwrap(MongoDatabase.class);
                }

                @Override
                public void dispose(MongoDatabase database) {

                }

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    return Collections.EMPTY_SET;
                }
            };
        }
    }

}
