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

package org.wildfly.extension.nosql.driver;

import java.lang.invoke.MethodHandle;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.nosql.common.MethodHandleBuilder;
import org.wildfly.nosql.common.NoSQLConstants;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OrientInteraction {

    private final Configuration configuration;
    private final Class oPartitionedDatabasePool;  // com.orientechnologies.orient.core.db.OPartitionedDatabasePool
    private MethodHandle oPartitionedDatabasePoolCtorMethod;

    public OrientInteraction(Configuration configuration) {
        this.configuration = configuration;
        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();
        // specify NoSQL driver classloader
        methodHandleBuilder.classLoader(ModuleIdentifier.fromString(configuration.getModuleName()));
        oPartitionedDatabasePool = methodHandleBuilder.className(NoSQLConstants.ORIENTDBPARTIONEDDBPOOLCLASS).getTargetClass();
        // OPartitionedDatabasePool(String url, String userName, String password, int maxPartitionSize, int maxPoolSize)
        oPartitionedDatabasePoolCtorMethod = methodHandleBuilder.declaredConstructor(
                String.class, String.class, String.class, int.class, int.class);

        methodHandleBuilder.className(NoSQLConstants.ORIENTDBDATABASERECORDTHREADLOCALCLASS);
        MethodHandle oDatabaseRecordThreadLocalInstanceField = methodHandleBuilder.findStaticField("INSTANCE");
        MethodHandle isDefinedMethod = methodHandleBuilder.method("isDefined");
        try {
            // call ODatabaseRecordThreadLocal.INSTANCE.isDefined(), which seems to be a bug. Needs call on INSTANCE to work later.
            // TODO: does this leak anything on the deployment thread?
            isDefinedMethod.invoke(oDatabaseRecordThreadLocalInstanceField.invoke());
        } catch (Throwable throwable) {
            throw new RuntimeException("could not reference " +methodHandleBuilder.getTargetClass().getName() + " INSTANCE field", throwable);
        }
    }

    <T> T getDatabasePool() {
        try {
            return (T)oPartitionedDatabasePoolCtorMethod.invoke(configuration.getDatabaseUrl(), configuration.getUserName(),
                    configuration.getPassword(), configuration.getMaxPartitionSize(), configuration.getMaxPoolSize());
        } catch (Throwable throwable) {
            throw new RuntimeException("could not create partitioned database connection pool for " + configuration.getDatabaseUrl() + " " + configuration.getUserName(), throwable);
        }
    }

    public Class getDatabasePoolClass() {
        return oPartitionedDatabasePool;
    }
}
