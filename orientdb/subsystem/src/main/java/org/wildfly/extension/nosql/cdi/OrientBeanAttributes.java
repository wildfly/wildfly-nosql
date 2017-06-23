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
