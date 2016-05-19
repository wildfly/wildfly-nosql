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

import javax.enterprise.context.ApplicationScoped;
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

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.wildfly.nosql.common.ConnectionServiceAccess;
import org.wildfly.nosql.common.spi.NoSQLConnection;


/**
 * This CDI Extension registers a <code>Mongoclient</code>
 * defined by adding a {@link MongoClientDefinition} annotation to any class of the application
 * Registration will be aborted if user defines her own <code>MongoClient</code> bean or producer
 *
 * @author Anttoine Sabot-Durand
 */
public class MongoExtension implements Extension {

    private static final Logger log = Logger.getLogger(MongoExtension.class.getName());
    private boolean moreThanOne = false;

    /**
     * Looks for {@link MongoClientDefinition} annotation to capture it.
     * Also Checks if the application contains more than one of these definition
     */
    void detectMongoClientDefinition(
            @Observes @WithAnnotations(MongoClientDefinition.class) ProcessAnnotatedType<?> pat) {
        AnnotatedType at = pat.getAnnotatedType();

        MongoClientDefinition md = at.getAnnotation(MongoClientDefinition.class);
        String name = md.name();

//        if (mongoDef != null) {
//            moreThanOne = true;
//        } else {
//            mongoDef = md;
//        }
    }

    /**
     * Warns user if there's none onr more than one {@link MongoClientDefinition} in the application
     */
    void checkMongoClientUniqueness(@Observes AfterTypeDiscovery atd) {
//        if (mongoDef == null) {
            log.warning("No MongoDB data sources found, mongo CDI extension will do nothing");
//        } else if (moreThanOne) {
//            log.log(Level.WARNING, "You defined more than one MongoDB data source. Only the one with name {0} will be "
//                    + "created", mongoDef
//                    .name());
//        }

    }

    /**
     * If the application has a {@link MongoClientDefinition} register the bean for it unless user has defined a bean or a
     * producer for a <code>MongoClient</code>
     */
    void registerDataSourceBeans(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        if (bm.getBeans(MongoClient.class, DefaultLiteral.INSTANCE).isEmpty()) {
//            log.log(Level.INFO, "Registering bean for MongoDB datasource {0}", mongoDef.name());

            abd.addBean(bm.createBean(new MongoClientBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType
                    (MongoClient.class))), MongoClient.class, new MongoClientProducerFactory()));

            abd.addBean(bm.createBean(new MongoDatabaseBeanAttributes(bm.createBeanAttributes(bm.createAnnotatedType
                                (MongoDatabase.class))), MongoDatabase.class, new MongoDatabaseProducerFactory()));
        } else {
            log.log(Level.INFO, "Application contains a default MongoClient Bean, automatic registration will be disabled");
        }
    }

    private static class MongoClientBeanAttributes implements BeanAttributes<MongoClient> {

        private BeanAttributes<MongoClient> delegate;

        MongoClientBeanAttributes(BeanAttributes<MongoClient> beanAttributes) {
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

        MongoClientProducerFactory() {
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
                    // TODO: remove hard coded profile name
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection("mongodbtestprofile");
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

        MongoDatabaseBeanAttributes(BeanAttributes<MongoDatabase> beanAttributes) {
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

        MongoDatabaseProducerFactory() {
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
                    // TODO: remove hard coded profile name
                    NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection("mongodbtestprofile");
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
