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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.nosql.common.MethodHandleBuilder;

/**
 * MongoInteraction
 *
 * @author Scott Marlow
 */
public class MongoInteraction {

    private final ArrayList serverAddressArrayList = new ArrayList(); // List<ServerAddress>
    private final ConfigurationBuilder configurationBuilder;
    private Object clientInstance;

    private static final String MONGOCLIENTCLASS = "com.mongodb.MongoClient";
    private final Class mongoClientClass;
    private static final String MONGOCLIENTOPTIONSCLASS = "com.mongodb.MongoClientOptions";
    private static final String MONGODATABASECLASS = "com.mongodb.client.MongoDatabase";

    private final Class mongoDatabaseClass;
    private final MethodHandle closeMethod;
    private final MethodHandle getDatabaseMethod;
    private final MethodHandle mongoClientCtorMethod;


    private static final String MONGOBUILDERCLASS = "com.mongodb.MongoClientOptions$Builder";
    private final MethodHandle builderCtorMethod;
    private final MethodHandle descriptionMethod;
    private final MethodHandle writeConcernMethod;
    private final MethodHandle buildMethod;

    private static final String MONGOWRITECONCERNCLASS = "com.mongodb.WriteConcern";
    private final MethodHandle writeConcernValueOfMethod;

    private static final String MONGOSERVERADDRESSCLASS = "com.mongodb.ServerAddress";
    private final MethodHandle serverAddressHostCtor;
    private final MethodHandle serverAddressHostPortCtor;

    public MongoInteraction(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();

        // specify NoSQL driver classloader
        methodHandleBuilder.classLoader(ModuleIdentifier.fromString(configurationBuilder.getModuleName()));
        // get MongoClientOptions class for creating MongoClient constructor
        Class mongoClientOptionsClass = methodHandleBuilder.className(MONGOCLIENTOPTIONSCLASS).getTargetClass();
        // save MongoClient class  so getter method can return it
        mongoClientClass = methodHandleBuilder.className(MONGOCLIENTCLASS).getTargetClass();
        closeMethod = methodHandleBuilder.method("close");
        getDatabaseMethod = methodHandleBuilder.declaredMethod("getDatabase", String.class);
        mongoClientCtorMethod = methodHandleBuilder.declaredConstructor(List.class, mongoClientOptionsClass);

        Class mongoWriteConcernClass = methodHandleBuilder.className(MONGOWRITECONCERNCLASS).getTargetClass();
        writeConcernValueOfMethod = methodHandleBuilder.method("valueOf", String.class);

        methodHandleBuilder.className(MONGOBUILDERCLASS);
        builderCtorMethod = methodHandleBuilder.constructor(MethodType.methodType(void.class));
        descriptionMethod = methodHandleBuilder.declaredMethod("description", String.class);
        writeConcernMethod = methodHandleBuilder.method("writeConcern", mongoWriteConcernClass);
        buildMethod = methodHandleBuilder.method("build");

        methodHandleBuilder.className(MONGOSERVERADDRESSCLASS);
        serverAddressHostCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, String.class));
        serverAddressHostPortCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, String.class, int.class));

        mongoDatabaseClass = methodHandleBuilder.className(MONGODATABASECLASS).getTargetClass();
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
        return mongoClient(serverAddressArrayList, mongoClientOptions);
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
        // MongoClientOptions mongoClientOptions = builder.build();
        // Object mongoClientOptions = buildMethod.invokeExact(builder);
        Object mongoClientOptions = buildMethod.invoke(builder);
        return mongoClientOptions;
    }

    // MongoClient(final List<ServerAddress> seeds, final MongoClientOptions mongoClientOptions) {
    // mongoClientOptions = MongoClientOptionsBuilderInteraction.mongoClientOptions()
    public Object mongoClient(Object serverAddressList, Object mongoClientOptions) throws Throwable {
        // return mongoClientCtorMethod.invokeExact(serverAddressList, mongoClientOptions);
        clientInstance = mongoClientCtorMethod.invoke(serverAddressList, mongoClientOptions);
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

}
