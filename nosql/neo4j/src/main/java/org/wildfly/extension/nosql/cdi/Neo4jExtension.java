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

import org.jboss.as.server.CurrentServiceContainer;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.wildfly.extension.nosql.subsystem.neo4j.Neo4jSubsystemService;
import org.wildfly.nosql.common.ConnectionServiceAccess;
import org.wildfly.nosql.common.SubsystemService;
import org.wildfly.nosql.common.spi.NoSQLConnection;


/**
 * This CDI Extension registers a <code>Driver</code>
 * defined by CDI Inject in application beans
 * Registration will be aborted if user defines her own <code>Driver</code> bean or producer
 *
 * TODO: eliminate dependency on client classes so different driver modules can be used.
 *
 * @author Antoine Sabot-Durand
 * @author Scott Marlow
 */
public class Neo4jExtension implements Extension {

    private static final Logger log = Logger.getLogger(Neo4jExtension.class.getName());

    void registerNoSQLSourceBeans(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        if (bm.getBeans(Driver.class, DefaultLiteral.INSTANCE).isEmpty()) {
            for(String profile: getService().profileNames()) {
                log.log(Level.INFO, "Registering bean for profile {0}", profile);
                abd.addBean(bm.createBean(
                        new DriverBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType(Driver.class)), profile),
                        Driver.class, new DriverProducerFactory(profile)));
                abd.addBean(bm.createBean(new SessionBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType(Session.class)),profile),
                        Session.class, new SessionProducerFactory(profile)));
            }
         } else {
            log.log(Level.INFO, "Application contains a default Driver Bean, automatic registration will be disabled");
        }
    }

    private SubsystemService getService() {
        return (SubsystemService) CurrentServiceContainer.getServiceContainer().getService(Neo4jSubsystemService.serviceName()).getValue();
    }

    private static class DriverBeanAttributes implements BeanAttributes<Driver> {

        private BeanAttributes<Driver> delegate;
        private final String profile;

        DriverBeanAttributes(BeanAttributes<Driver> beanAttributes, String profile) {
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
            NamedLiteral namedLiteral = new NamedLiteral(profile);
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

    private static class DriverProducerFactory
            implements InjectionTargetFactory<Driver> {
        private final String profile;

        DriverProducerFactory(String profile) {
            this.profile = profile;
        }

        @Override
        public InjectionTarget<Driver> createInjectionTarget(Bean<Driver> bean) {
            return new InjectionTarget<Driver>() {
                @Override
                public void inject(Driver instance, CreationalContext<Driver> ctx) {
                }

                @Override
                public void postConstruct(Driver instance) {
                }

                @Override
                public void preDestroy(Driver instance) {
                }

                @Override
                public Driver produce(CreationalContext<Driver> ctx) {
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection(profile);
                    return noSQLConnection.unwrap(Driver.class);
                }

                @Override
                public void dispose(Driver driver) {
                    // no need to close the shared driver, its thread safe and shared by all application deployments.
                }

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    return Collections.EMPTY_SET;
                }
            };
        }
    }

    private static class SessionBeanAttributes implements BeanAttributes<Session> {

        private BeanAttributes<Session> delegate;
        private final String profile;

        SessionBeanAttributes(BeanAttributes<Session> beanAttributes, String profile) {
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
            NamedLiteral namedLiteral = new NamedLiteral(profile);
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

    private static class SessionProducerFactory
            implements InjectionTargetFactory<Session> {
        private final String profile;

        SessionProducerFactory(String profile) {
            this.profile = profile;
        }

        @Override
        public InjectionTarget<Session> createInjectionTarget(Bean<Session> bean) {
            return new InjectionTarget<Session>() {
                @Override
                public void inject(Session instance, CreationalContext<Session> ctx) {
                }

                @Override
                public void postConstruct(Session instance) {
                }

                @Override
                public void preDestroy(Session instance) {
                }

                @Override
                public Session produce(CreationalContext<Session> ctx) {
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection(profile);
                    return noSQLConnection.unwrap(Session.class);
                }

                /**
                 * Injected session should be closed as soon as RequestScoped request completes,
                 * to ensure that the (not thread safe) session can then be used for another request.
                 *
                 * @param session
                 */
                @Override
                public void dispose(Session session) {
                    session.close();
                }

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    return Collections.EMPTY_SET;
                }
            };
        }
    }

}
