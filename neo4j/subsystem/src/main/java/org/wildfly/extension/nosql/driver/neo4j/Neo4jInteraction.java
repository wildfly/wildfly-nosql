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

package org.wildfly.extension.nosql.driver.neo4j;

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Set;

import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.StartException;
import org.jboss.security.SubjectFactory;
import org.wildfly.nosql.common.MethodHandleBuilder;
import org.wildfly.nosql.common.NoSQLConstants;

/**
 * Neo4jInteraction is for interacting with Neo4j without static references to Neo4j classes.
 *
 * @author Scott Marlow
 */
public class Neo4jInteraction {

    private StringBuffer builder = new StringBuffer("");
    private final Class driverClass;
    private final MethodHandle closeDriverMethod;
    private final MethodHandle buildMethod;
    private final MethodHandle buildWithAuthMethod;
    private final MethodHandle basicAuthMethod;
    private volatile SubjectFactory subjectFactory;
    private final String securityDomain;

    public Neo4jInteraction(ConfigurationBuilder configurationBuilder) {
        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();
        // specify NoSQL driver classloader
        methodHandleBuilder.classLoader(ModuleIdentifier.fromString(configurationBuilder.getModuleName()));

        // auth handling
        Class authTokenClass = methodHandleBuilder.className(NoSQLConstants.NEO4JAUTHTOKENCLASS).getTargetClass();
        methodHandleBuilder.className(NoSQLConstants.NEO4JAUTHTOKENSCLASS).getTargetClass();
        basicAuthMethod = methodHandleBuilder.staticMethod( "basic", MethodType.methodType(authTokenClass, String.class,String.class));

        driverClass = methodHandleBuilder.className(NoSQLConstants.NEO4JDRIVERCLASS).getTargetClass();
        closeDriverMethod = methodHandleBuilder.method("close");
        methodHandleBuilder.className(NoSQLConstants.NEO4JGRAPHDATABASECLASS);
        buildMethod = methodHandleBuilder.staticMethod("driver", MethodType.methodType(driverClass, String.class));
        // builder with auth
        buildWithAuthMethod = methodHandleBuilder.staticMethod("driver", MethodType.methodType(driverClass, String.class, authTokenClass));
        securityDomain = configurationBuilder.getSecurityDomain();
    }

    public void subjectFactory(SubjectFactory subjectFactory) {
        this.subjectFactory = subjectFactory;
    }

    protected Object /* Driver */ build() throws Throwable {
        if(securityDomain != null && subjectFactory != null) {
            try {
                Subject subject = subjectFactory.createSubject(securityDomain);
                Set<PasswordCredential> passwordCredentials = subject.getPrivateCredentials(PasswordCredential.class);
                PasswordCredential passwordCredential = passwordCredentials.iterator().next();
                // driver( String url, AuthToken authToken)
                return buildWithAuthMethod.invoke(builder.toString(),
                        basicAuthMethod.invoke(passwordCredential.getUserName(), new String(passwordCredential.getPassword())));
            } catch(Throwable problem) {
                if (ROOT_LOGGER.isTraceEnabled()) {
                    ROOT_LOGGER.tracef(problem,"could not create subject for security domain '%s'",
                            securityDomain);
                }
                throw problem;
            }
        }
        else
            // driver( String url)
            return buildMethod.invoke(builder.toString());
    }

    protected void withPort(int port) throws StartException {
        add(":");
        add(Integer.toString(port));
    }

    protected void addContactPoint(String host) throws StartException {
        add(host);
    }

    private void add(String value) {
        if(builder.length() == 0) {
            builder.append("bolt://");
        }
        builder.append(value);
    }

    protected void driverClose(Object driver) throws Throwable {
        closeDriverMethod.invoke(driver);
    }

    protected Class getDriverClass() {
        return driverClass;
    }


}
