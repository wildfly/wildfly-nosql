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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * This Qualifier is used to qualify an injected MongoDB element and to contain meta-data about the requested element used by
 * the corresponding producer in {@link MongoProducers}
 *
 * TODO: This class has mostly been replaced by ClientProfile.  Should MongoProducers reference ClientProfile?
 * We likely will add some MongoDB specific extensions (e.g. customize read/write concern), which this interface might be a
 * good placeholder example for.
 *
 * @author Antoine Sabot-Durand
 */

@Target(value = {TYPE, METHOD, PARAMETER, FIELD})
@Retention(value = RUNTIME)
@Documented
@Qualifier
public @interface Mongo {

    @Nonbinding String profile() default "default";
    /**
     * @return the name of the MongoDB database for the injection point
     */
    @Nonbinding String db() default "default";

    /**
     * @return the name of the MongoDB collection for the injection point
     */
    @Nonbinding String collection() default "default";
}
