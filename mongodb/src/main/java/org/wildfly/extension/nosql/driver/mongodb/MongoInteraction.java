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

package org.wildfly.extension.nosql.driver.mongodb;

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.modules.ModuleIdentifier;
import org.jboss.security.SubjectFactory;
import org.wildfly.nosql.common.MethodHandleBuilder;
import org.wildfly.nosql.common.NoSQLConstants;

/**
 * MongoInteraction
 *
 * @author Scott Marlow
 */
public class MongoInteraction {

    private final ArrayList serverAddressArrayList = new ArrayList(); // List<ServerAddress>
    private final ConfigurationBuilder configurationBuilder;
    private Object clientInstance;

    private final Class mongoClientClass;
    private final Class mongoDatabaseClass;
    private final MethodHandle closeMethod;
    private final MethodHandle getDatabaseMethod;
    private final MethodHandle mongoClientCtorMethod;
    private final MethodHandle mongoClientSecurityCtorMethod;

    private final MethodHandle builderCtorMethod;
    private final MethodHandle descriptionMethod;
    private final MethodHandle writeConcernMethod;
    private final MethodHandle readConcernMethod;
    private final MethodHandle buildMethod;

    private final MethodHandle writeConcernValueOfMethod;
    private final MethodHandle readConcernCtorMethod;
    private final MethodHandle readConcernLevelFromStringMethod;

    private final MethodHandle serverAddressHostCtor;
    private final MethodHandle serverAddressHostPortCtor;

    private final Class mongoCredentialClass;
    private final MethodHandle mongoCredentialCreateCredential;
    private volatile SubjectFactory subjectFactory;

    public MongoInteraction(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();

        // specify NoSQL driver classloader
        methodHandleBuilder.classLoader(ModuleIdentifier.fromString(configurationBuilder.getModuleName()));
        // get MongoClientOptions class for creating MongoClient constructor
        Class mongoClientOptionsClass = methodHandleBuilder.className(NoSQLConstants.MONGOCLIENTOPTIONSCLASS).getTargetClass();
        // save MongoClient class  so getter method can return it
        mongoClientClass = methodHandleBuilder.className(NoSQLConstants.MONGOCLIENTCLASS).getTargetClass();
        closeMethod = methodHandleBuilder.method("close");
        getDatabaseMethod = methodHandleBuilder.declaredMethod("getDatabase", String.class);
        mongoClientCtorMethod = methodHandleBuilder.declaredConstructor(List.class, mongoClientOptionsClass);
        // MongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList, final MongoClientOptions options)
        mongoClientSecurityCtorMethod = methodHandleBuilder.declaredConstructor(List.class, List.class, mongoClientOptionsClass);

        Class mongoWriteConcernClass = methodHandleBuilder.className(NoSQLConstants.MONGOWRITECONCERNCLASS).getTargetClass();
        writeConcernValueOfMethod = methodHandleBuilder.method("valueOf", String.class);

        Class mongoReadConcernLevelClass = methodHandleBuilder.className(NoSQLConstants.MONGOREADCONCERNLEVELCLASS).getTargetClass();
        readConcernLevelFromStringMethod = methodHandleBuilder.method("fromString", String.class);

        Class mongoReadConcernClass = methodHandleBuilder.className(NoSQLConstants.MONGOREADCONCERNCLASS).getTargetClass();
        readConcernCtorMethod = methodHandleBuilder.declaredConstructor(mongoReadConcernLevelClass);

        methodHandleBuilder.className(NoSQLConstants.MONGOBUILDERCLASS);
        builderCtorMethod = methodHandleBuilder.constructor(MethodType.methodType(void.class));
        descriptionMethod = methodHandleBuilder.declaredMethod("description", String.class);
        writeConcernMethod = methodHandleBuilder.method("writeConcern", mongoWriteConcernClass);
        readConcernMethod = methodHandleBuilder.method("readConcern", mongoReadConcernClass);
        buildMethod = methodHandleBuilder.method("build");

        methodHandleBuilder.className(NoSQLConstants.MONGOSERVERADDRESSCLASS);
        serverAddressHostCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, String.class));
        serverAddressHostPortCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, String.class, int.class));

        mongoDatabaseClass = methodHandleBuilder.className(NoSQLConstants.MONGODATABASECLASS).getTargetClass();

        mongoCredentialClass = methodHandleBuilder.className(NoSQLConstants.MONGOCREDENTIALCLASS).getTargetClass();
        // public static MongoCredential createCredential(final String userName, final String database, final char[] password) {
        mongoCredentialCreateCredential = methodHandleBuilder.staticMethod
                ("createCredential", MethodType.methodType(mongoCredentialClass, String.class, String.class, char[].class));
    }

    public void hostPort(String host, int port) throws Throwable {
        if (port > 0) {
            serverAddressArrayList.add(serverAddressHostPortCtor.invoke(host, port));
        } else {
            serverAddressArrayList.add(serverAddressHostCtor.invoke(host));
        }
    }

    public Object /*MongoClient*/ mongoClient() throws Throwable {
        Object mongoClientOptions = mongoClientOptions();
        List mongoCredential = null;
        if (configurationBuilder.getSecurityDomain() != null) {
            mongoCredential = mongoCredential();
        }
        return mongoClient(serverAddressArrayList, mongoClientOptions, mongoCredential);
    }

    public Object getDB() throws Throwable {
        return getDatabase(configurationBuilder.getDatabase());
    }

    public void close() throws Throwable {
        underlyingClose();
    }

    public Object /* MongoClientOptions */ mongoClientOptions() throws Throwable {
        Object builder = builderCtorMethod.invoke();
        // builder.description(configurationBuilder.getDescription());
        //descriptionMethod.invokeExact(builder, configurationBuilder.getDescription());
        descriptionMethod.invoke(builder, configurationBuilder.getDescription());
        String writeConcernName = configurationBuilder.getWriteConcern();
        if (writeConcernName != null) {
            // public static WriteConcern valueOf(final String name)
            Object writeConcernValue = writeConcernValueOfMethod.invoke(writeConcernName);
            writeConcernMethod.invoke(builder, writeConcernValue);
        }
        if (configurationBuilder.getReadConcern() != null) {
            Object readConcernLevelValue = readConcernLevelFromStringMethod.invoke(configurationBuilder.getReadConcern());
            Object readConcernValue = readConcernCtorMethod.invoke(readConcernLevelValue);
            readConcernMethod.invoke(builder, readConcernValue);
        }
        // MongoClientOptions mongoClientOptions = builder.build();
        // Object mongoClientOptions = buildMethod.invokeExact(builder);
        Object mongoClientOptions = buildMethod.invoke(builder);
        return mongoClientOptions;
    }

    // public <U> Class<? extends U> asSubclass(Class<U> clazz) {
    public List /* MongoCredential */ mongoCredential() throws Throwable {
        List resultList = null;
        if (configurationBuilder.getSecurityDomain() != null && subjectFactory != null) {
            try {
                Subject subject = subjectFactory.createSubject(configurationBuilder.getSecurityDomain());
                Set<PasswordCredential> passwordCredentials = subject.getPrivateCredentials(PasswordCredential.class);
                PasswordCredential passwordCredential = passwordCredentials.iterator().next();
                // public static MongoCredential createCredential(final String userName, final String database, final char[] password) {
                if (resultList == null) {
                    resultList = new ArrayList();
                }
                Object result = mongoCredentialCreateCredential.invoke(passwordCredential.getUserName(), configurationBuilder.getDatabase(), passwordCredential.getPassword());
                resultList.add(result);
            } catch(Throwable problem) {
                if (ROOT_LOGGER.isTraceEnabled()) {
                    ROOT_LOGGER.tracef(problem,"could not create subject for security domain '%s' with database '%s'",
                            configurationBuilder.getSecurityDomain(), configurationBuilder.getDatabase());
                }
                throw problem;
            }
        }
        return resultList;
    }

    // MongoClient(final List<ServerAddress> seeds, final MongoClientOptions mongoClientOptions) {
    // mongoClientOptions = MongoClientOptionsBuilderInteraction.mongoClientOptions()
    public Object mongoClient(Object serverAddressList, Object mongoClientOptions, List mongoCredential) throws Throwable {

        if (mongoCredential != null && mongoCredential.size() > 0) {
            clientInstance = mongoClientSecurityCtorMethod.invoke(serverAddressList, castCollection(mongoCredential, mongoCredentialClass), mongoClientOptions);
        }
        else {
            // return mongoClientCtorMethod.invokeExact(serverAddressList, mongoClientOptions);
            clientInstance = mongoClientCtorMethod.invoke(serverAddressList, mongoClientOptions);
        }
        return clientInstance;
    }

    public Object getDatabase(String databaseName) throws Throwable {
        Object result = null;
        if(clientInstance != null) {
            // result = getDatabaseMethod.invokeExact(clientInstance, databaseName);
            result = getDatabaseMethod.invoke(clientInstance, databaseName);
        }
        return result;
    }

    public void underlyingClose() throws Throwable {
        try {
            if (clientInstance != null) {
                // closeMethod.invokeExact(clientInstance);
                closeMethod.invoke(clientInstance);
            }
        } finally {
            clientInstance = null;
        }
    }

    public Class getMongoClientClass() {
        return mongoClientClass;
    }

    public Class getMongoDatabaseClass() {
        return mongoDatabaseClass;
    }

    public void subjectFactory(SubjectFactory subjectFactory) {
        this.subjectFactory = subjectFactory;
    }

    private <X> List<X> castCollection(List srcList, Class<X> xClass) {
        List<X> arrayList = new ArrayList<X>();
        for (Object srcObject : srcList) {
            if (srcObject != null && xClass.isAssignableFrom(srcObject.getClass()))
                arrayList.add(xClass.cast(srcObject));
        }
        return arrayList;
    }
}
