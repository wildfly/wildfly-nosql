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

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.lang.invoke.MethodHandle;
import java.util.Set;

import javax.security.auth.Subject;
import javax.resource.spi.security.PasswordCredential;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.security.SubjectFactory;
import org.wildfly.nosql.common.MethodHandleBuilder;
import org.wildfly.nosql.common.NoSQLConstants;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OrientInteraction {

    private final Configuration configuration;
    private final Class oPartitionedDatabasePool;  // com.orientechnologies.orient.core.db.OPartitionedDatabasePool
    private MethodHandle oPartitionedDatabasePoolCtorMethod;
    private volatile SubjectFactory subjectFactory;

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
        MethodHandle oDatabaseRecordThreadLocalInstanceField = methodHandleBuilder.staticField("INSTANCE");
        MethodHandle isDefinedMethod = methodHandleBuilder.method("isDefined");
        try {
            // call ODatabaseRecordThreadLocal.INSTANCE.isDefined(), which seems to be a bug. Needs call on INSTANCE to work later.
            // TODO: does this leak anything on the deployment thread?
            isDefinedMethod.invoke(oDatabaseRecordThreadLocalInstanceField.invoke());
        } catch (Throwable throwable) {
            throw new RuntimeException("could not reference " +methodHandleBuilder.getTargetClass().getName() + " INSTANCE field", throwable);
        }
    }
    public void subjectFactory(SubjectFactory subjectFactory) {
            this.subjectFactory = subjectFactory;
        }

    <T> T getDatabasePool() {
        String username = null;
        String password = null;
        if (configuration.getSecurityDomain() != null && subjectFactory != null) {
            try {
                Subject subject = subjectFactory.createSubject(configuration.getSecurityDomain());
                Set<PasswordCredential> passwordCredentials = subject.getPrivateCredentials(PasswordCredential.class);
                PasswordCredential passwordCredential = passwordCredentials.iterator().next();
                username = passwordCredential.getUserName();
                password = new String(passwordCredential.getPassword());
            } catch (Throwable problem) {
                if (ROOT_LOGGER.isTraceEnabled()) {
                    ROOT_LOGGER.tracef(problem, "could not create subject for security domain '%s', user '%s', with '%s'",
                            configuration.getSecurityDomain(), username, configuration.getDatabaseUrl());
                }
                throw problem;
            }
        }
        try {

            return (T)oPartitionedDatabasePoolCtorMethod.invoke(configuration.getDatabaseUrl(), username,
                    password, configuration.getMaxPartitionSize(), configuration.getMaxPoolSize());
        } catch (Throwable throwable) {
            username = password = null;
            throw new RuntimeException("could not create partitioned database connection pool for " + configuration.getDatabaseUrl() + " " + username, throwable);
        }
    }

    public Class getDatabasePoolClass() {
        return oPartitionedDatabasePool;
    }
}
