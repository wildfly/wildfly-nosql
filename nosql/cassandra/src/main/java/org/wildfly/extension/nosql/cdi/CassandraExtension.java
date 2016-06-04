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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;  // TODO: verify if Dependent scope is okay, instead of ApplicationScoped (which got below error)
                                            // WELD-001435: Normal scoped bean class com.datastax.driver.core.Cluster is not proxyable because it has no no-args constructor...
                                            // unit test passes with Dependent scope.
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.wildfly.nosql.ClientProfile;
import org.wildfly.nosql.common.ConnectionServiceAccess;
import org.wildfly.nosql.common.spi.NoSQLConnection;


/**
 * This CDI Extension registers a <code>Session,Cluster</code>
 * defined by adding a {@link ClientProfile} annotation to any class of the application
 * Registration will be aborted if user defines her own <code>Session,Cluster</code> bean or producer
 *
 * TODO: eliminate dependency on Cassandra client classes so different Cassandra driver modules can be used.
 *
 * @author Antoine Sabot-Durand
 * @author Scott Marlow
 */
public class CassandraExtension implements Extension {

    private static final Logger log = Logger.getLogger(CassandraExtension.class.getName());
    private ClientProfile clientProfileDef = null;
    private boolean moreThanOne = false;

    /**
     * Looks for {@link ClientProfile} annotation to capture it.
     * Also Checks if the application contains more than one of these definition
     */
    void detectClientProfileDefinition(
            @Observes @WithAnnotations(ClientProfile.class) ProcessAnnotatedType<?> pat) {
        AnnotatedType at = pat.getAnnotatedType();

        ClientProfile md = at.getAnnotation(ClientProfile.class);

        if (clientProfileDef != null) {
            moreThanOne = true;
        } else {
            clientProfileDef = md;
        }
    }

    /**
     * Warns user if there's none onr more than one {@link ClientProfile} in the application
     */
    void checkCassandraClientUniqueness(@Observes AfterTypeDiscovery atd) {
        if (clientProfileDef == null) {
            log.warning("No @ClientProfile found, extension will do nothing");
        } else if (moreThanOne) {
            log.log(Level.WARNING, "You defined more than one @ClientProfile. Only the one with profile {0} will be "
                    + "created", clientProfileDef
                    .profile());
        }

    }

    /**
     * If the application has a {@link ClientProfile} register the bean for it unless user has defined a bean or a
     * producer for a <code>Cluster</code>
     */
    void registerNoSQLSourceBeans(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        if (clientProfileDef != null) {
            if (bm.getBeans(Cluster.class, DefaultLiteral.INSTANCE).isEmpty()) {
                log.log(Level.INFO, "Registering bean for ClientProfile profile {0}", clientProfileDef.profile());
                abd.addBean(bm.createBean(new ClusterBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType
                        (Cluster.class))), Cluster.class, new ClusterProducerFactory(clientProfileDef.profile(), clientProfileDef.lookup())));
                abd.addBean(bm.createBean(new SessionBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType
                        (Session.class))), Session.class, new SessionProducerFactory(clientProfileDef.profile(), clientProfileDef.lookup())));
             } else {
                log.log(Level.INFO, "Application contains a default Cluster Bean, automatic registration will be disabled");
            }
        }
    }

    private static class ClusterBeanAttributes implements BeanAttributes<Cluster> {

        private BeanAttributes<Cluster> delegate;

        ClusterBeanAttributes(BeanAttributes<Cluster> beanAttributes) {
            delegate = beanAttributes;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return delegate.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
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

    private static class ClusterProducerFactory
            implements InjectionTargetFactory<Cluster> {
        String profile, jndi;

        ClusterProducerFactory(String profile, String jndi) {
            this.profile = profile;
            this.jndi = jndi;
        }

        @Override
        public InjectionTarget<Cluster> createInjectionTarget(Bean<Cluster> bean) {
            return new InjectionTarget<Cluster>() {
                @Override
                public void inject(Cluster instance, CreationalContext<Cluster> ctx) {
                }

                @Override
                public void postConstruct(Cluster instance) {
                }

                @Override
                public void preDestroy(Cluster instance) {
                }

                @Override
                public Cluster produce(CreationalContext<Cluster> ctx) {
                    // TODO: use jndi if profile is null
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection(profile);
                    return noSQLConnection.unwrap(Cluster.class);
                }

                @Override
                public void dispose(Cluster connection) {
                    // connection.close();
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

        SessionBeanAttributes(BeanAttributes<Session> beanAttributes) {
            delegate = beanAttributes;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return delegate.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
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
        String profile, jndi;

        SessionProducerFactory(String profile, String jndi) {
            this.profile = profile;
            this.jndi = jndi;
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
                    // TODO: use jndi if profile is null
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection(profile);
                    return noSQLConnection.unwrap(Session.class);
                }

                @Override
                public void dispose(Session database) {

                }

                @Override
                public Set<InjectionPoint> getInjectionPoints() {
                    return Collections.EMPTY_SET;
                }
            };
        }
    }

}
