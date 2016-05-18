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

package org.wildfly.nosql.common.cdi;

import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;

import org.wildfly.nosql.common.ConnectionServiceAccess;
import org.wildfly.nosql.common.spi.NoSQLConnection;

/**
 * ClientProducerFactory
 *
 * @author Scott Marlow
 */
public class ClientProducerFactory implements InjectionTargetFactory<NoSQLConnection> {

    private String targetId;

    public ClientProducerFactory(String targetId) {
        this.targetId = targetId;
    }

    @Override
    public InjectionTarget<NoSQLConnection> createInjectionTarget(Bean<NoSQLConnection> bean) {
        return new InjectionTarget<NoSQLConnection>() {
            @Override
            public void inject(NoSQLConnection instance, CreationalContext<NoSQLConnection> ctx) {
            }

            @Override
            public void postConstruct(NoSQLConnection instance) {
            }

            @Override
            public void preDestroy(NoSQLConnection instance) {
            }

            @Override
            public NoSQLConnection produce(CreationalContext<NoSQLConnection> ctx) {
                // return new MongoClient(uri);
                return ConnectionServiceAccess.connection(targetId);
            }

            @Override
            public void dispose(NoSQLConnection instance) {
                // instance.close();
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return Collections.EMPTY_SET;
            }
        };
    }
}

