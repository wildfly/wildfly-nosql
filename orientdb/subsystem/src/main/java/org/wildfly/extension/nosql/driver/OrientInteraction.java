/*
 * *
 *  * Copyright 2017 Red Hat, Inc, and individual contributors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
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
    private MethodHandle oPartitionedDatabasePoolDefaultSizeCtorMethod;
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
        oPartitionedDatabasePoolDefaultSizeCtorMethod = methodHandleBuilder.declaredConstructor(
                        String.class, String.class, String.class);
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
            if(configuration.getMaxPartitionSize() > 0 || configuration.getMaxPoolSize() > 0)
                return (T)oPartitionedDatabasePoolCtorMethod.invoke(configuration.getDatabaseUrl(), username,
                    password, configuration.getMaxPartitionSize(), configuration.getMaxPoolSize());
            else
                return (T)oPartitionedDatabasePoolDefaultSizeCtorMethod.invoke(configuration.getDatabaseUrl(), username,
                                    password);
        } catch (Throwable throwable) {
            username = password = null;
            throw new RuntimeException("could not create partitioned database connection pool for " + configuration.getDatabaseUrl() + " " + username, throwable);
        }
    }

    public Class getDatabasePoolClass() {
        return oPartitionedDatabasePool;
    }
}
