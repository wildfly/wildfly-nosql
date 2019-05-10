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
    private final MethodHandle sslEnabledMethod;
    private final MethodHandle replicaSetMethod;
    private final MethodHandle buildMethod;

    private final MethodHandle writeConcernValueOfMethod;
    private final MethodHandle readConcernCtorMethod;
    private final MethodHandle readConcernLevelFromStringMethod;

    private final MethodHandle serverAddressHostCtor;
    private final MethodHandle serverAddressHostPortCtor;

    private final Class mongoCredentialClass;
    private final MethodHandle mongoCredentialCreateCredential;
    private final MethodHandle mongoCredentialGSSAPICreateCredential;
    private final MethodHandle mongoCredentialMongoCRCreateCredential;
    private final MethodHandle mongoCredentialX509CreateCredential;
    private final MethodHandle mongoCredentialPlainCreateCredential;
    private final MethodHandle mongoCredentialScramSha1CreateCredential;
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
        sslEnabledMethod = methodHandleBuilder.method("sslEnabled", boolean.class);
        replicaSetMethod = methodHandleBuilder.method("requiredReplicaSetName", String.class);
        buildMethod = methodHandleBuilder.method("build");

        methodHandleBuilder.className(NoSQLConstants.MONGOSERVERADDRESSCLASS);
        serverAddressHostCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, String.class));
        serverAddressHostPortCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, String.class, int.class));

        mongoDatabaseClass = methodHandleBuilder.className(NoSQLConstants.MONGODATABASECLASS).getTargetClass();

        mongoCredentialClass = methodHandleBuilder.className(NoSQLConstants.MONGOCREDENTIALCLASS).getTargetClass();
        // public static MongoCredential createCredential(final String userName, final String database, final char[] password) {
        mongoCredentialCreateCredential = methodHandleBuilder.staticMethod
                ("createCredential", MethodType.methodType(mongoCredentialClass, String.class, String.class, char[].class));
        // public static MongoCredential createGSSAPICredential(final String userName) {
        mongoCredentialGSSAPICreateCredential = methodHandleBuilder.staticMethod
                ( "createGSSAPICredential", MethodType.methodType(mongoCredentialClass, String.class));
        // public static MongoCredential createMongoCRCredential(final String userName, final String database, final char[] password) {
        mongoCredentialMongoCRCreateCredential = methodHandleBuilder.staticMethod
                ("createMongoCRCredential", MethodType.methodType(mongoCredentialClass, String.class, String.class, char[].class));
        // public static MongoCredential createMongoX509Credential(final String userName) {
        mongoCredentialX509CreateCredential = methodHandleBuilder.staticMethod
                ( "createMongoX509Credential", MethodType.methodType(mongoCredentialClass, String.class));
        // public static MongoCredential createPlainCredential(final String userName, final String database, final char[] password) {
        mongoCredentialPlainCreateCredential = methodHandleBuilder.staticMethod
                ("createPlainCredential", MethodType.methodType(mongoCredentialClass, String.class, String.class, char[].class));
        // public static MongoCredential createScramSha1Credential(final String userName, final String database, final char[] password) {
        mongoCredentialScramSha1CreateCredential = methodHandleBuilder.staticMethod
                ("createScramSha1Credential", MethodType.methodType(mongoCredentialClass, String.class, String.class, char[].class));
    }

    public void hostPort(String host, int port) throws Throwable {
        if (port > 0) {
            serverAddressArrayList.add(serverAddressHostPortCtor.invoke(host, port));
        } else {
            serverAddressArrayList.add(serverAddressHostCtor.invoke(host));
        }
    }

    public Object /*MongoClient*/ mongoClient() throws Throwable {
        return mongoClient(serverAddressArrayList, mongoClientOptions(), mongoCredential());
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
        if (configurationBuilder.isSSL()) {
            sslEnabledMethod.invoke(builder,true);
        }
        if (configurationBuilder.getReplicaSet() != null) {
            // public Builder requiredReplicaSetName(final String requiredReplicaSetName)
            replicaSetMethod.invoke(builder, configurationBuilder.getReplicaSet());
        }
        // MongoClientOptions mongoClientOptions = builder.build();
        // Object mongoClientOptions = buildMethod.invokeExact(builder);
        Object mongoClientOptions = buildMethod.invoke(builder);
        return mongoClientOptions;
    }

    // public <U> Class<? extends U> asSubclass(Class<U> clazz) {
    public List /* MongoCredential */ mongoCredential() throws Throwable {

        if (configurationBuilder.getSecurityDomain() != null && subjectFactory != null) {
            // use the admin database if specified for authentication, otherwise use the application database.
            final String database = configurationBuilder.getAdminDatabase() != null ? configurationBuilder.getAdminDatabase() : configurationBuilder.getDatabase();
            try {
                Subject subject = subjectFactory.createSubject(configurationBuilder.getSecurityDomain());
                Set<PasswordCredential> passwordCredentials = subject.getPrivateCredentials(PasswordCredential.class);
                PasswordCredential passwordCredential = passwordCredentials.iterator().next();
                // public static MongoCredential createCredential(final String userName, final String database, final char[] password) {
                List resultList = new ArrayList();
                if(configurationBuilder.getAuthType() == null || AuthType.DEFAULT.equals(configurationBuilder.getAuthType())) {
                    resultList.add(mongoCredentialCreateCredential.invoke(passwordCredential.getUserName(), database, passwordCredential.getPassword()));
                    return resultList;
                }
                else if(AuthType.GSSAPI.equals(configurationBuilder.getAuthType())) {
                    // createGSSAPICredential( final String username )
                    resultList.add(mongoCredentialGSSAPICreateCredential.invoke(passwordCredential.getUserName()));
                    return resultList;
                }
                else if(AuthType.MONGODB_CR.equals(configurationBuilder.getAuthType())) {
                    resultList.add(mongoCredentialMongoCRCreateCredential.invoke(passwordCredential.getUserName(), database, passwordCredential.getPassword()));
                    return resultList;
                }
                else if(AuthType.MONGODB_X509.equals(configurationBuilder.getAuthType())) {
                    resultList.add(mongoCredentialX509CreateCredential.invoke(passwordCredential.getUserName()));
                    return resultList;
                }
                else if(AuthType.PLAIN_SASL.equals(configurationBuilder.getAuthType())) {
                    resultList.add(mongoCredentialPlainCreateCredential.invoke(passwordCredential.getUserName(), database, passwordCredential.getPassword()));
                    return resultList;
                }
                else if(AuthType.SCRAM_SHA_1.equals(configurationBuilder.getAuthType())) {
                    resultList.add(mongoCredentialScramSha1CreateCredential.invoke(passwordCredential.getUserName(), database, passwordCredential.getPassword()));
                    return resultList;
                }
                else {
                    throw new RuntimeException("unhandled auth-type " + configurationBuilder.getAuthType().toString());
                }
            } catch(Throwable problem) {
                if (ROOT_LOGGER.isTraceEnabled()) {
                    ROOT_LOGGER.tracef(problem,"could not create subject for security domain '%s' with database '%s'",
                            configurationBuilder.getSecurityDomain(), database);
                }
                throw problem;
            }
        }
        return null;
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
