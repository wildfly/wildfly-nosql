/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extension.nosql.cdi;

import org.wildfly.nosql.common.ConnectionServiceAccess;
import org.wildfly.nosql.common.spi.NoSQLConnection;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class OrientInjectionTarget<T> implements InjectionTarget<T> {

    private final Class<T> beanClass;

    private final String profile;

    OrientInjectionTarget(Class<T> beanClass, String profile) {
        this.beanClass = beanClass;
        this.profile = profile;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {

    }

    @Override
    public void postConstruct(T instance) {

    }

    @Override
    public void preDestroy(T instance) {

    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        NoSQLConnection noSQLConnection = ConnectionServiceAccess.connection(profile);
        return noSQLConnection.unwrap(beanClass);
    }

    @Override
    public void dispose(T instance) {

    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

}
