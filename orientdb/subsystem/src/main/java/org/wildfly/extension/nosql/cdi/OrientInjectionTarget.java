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
