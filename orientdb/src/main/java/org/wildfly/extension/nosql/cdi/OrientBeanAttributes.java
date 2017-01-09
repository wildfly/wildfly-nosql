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

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanAttributes;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class OrientBeanAttributes<T> implements BeanAttributes<T> {

    private final BeanAttributes<T> delegate;

    private final String profile;

    OrientBeanAttributes(BeanAttributes<T> delegate, String profile) {
        this.delegate = delegate;
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
