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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;

/**
 * MongoInteraction
 *
 * @author Scott Marlow
 */
public class MongoInteraction {

    private final ArrayList serverAddressArrayList = new ArrayList(); // List<ServerAddress>
    private final ConfigurationBuilder configurationBuilder;
    private final ClassLoader driverClassLoader;

    private static final String MONGOCLIENTCLASS = "com.mongodb.MongoClient";
    private final Class mongoClientClass;
    private static final String MONGOCLIENTOPTIONSCLASS = "com.mongodb.MongoClientOptions";
    private final Class mongoClientOptionsClass;
    private static final String MONGODATABASECLASS = "com.mongodb.client.MongoDatabase";
    private final Class mongoDatabaseClass;

    private final MethodHandle closeMethod;
    private final MethodHandle getDatabaseMethod;
    private final MethodHandle mongoClientCtorMethod;
    private Object clientInstance;

    private static final String MONGOBUILDERCLASS = "com.mongodb.MongoClientOptions$Builder";
    private final Class mongoBuilderClass;
    private static final String MONGOWRITECONCERNCLASS = "com.mongodb.WriteConcern";
    private final Class mongoWriteConcernClass;
    private final MethodHandle builderCtorMethod;
    private final MethodHandle descriptionMethod;
    private final MethodHandle writeConcernMethod;
    private final MethodHandle writeConcernValueOfMethod;
    private final MethodHandle buildMethod;

    private static final String MONGOSERVERADDRESSCLASS = "com.mongodb.ServerAddress";
    private final Class mongoServerAddressClass;
    private final MethodHandle serverAddressHostCtor;
    private final MethodHandle serverAddressHostPortCtor;

    public MongoInteraction(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();
        try {
            Module module = moduleLoader.loadModule(ModuleIdentifier.fromString(configurationBuilder.getModuleName()));
            driverClassLoader = module.getClassLoader();
        } catch (ModuleLoadException e) {
            throw new RuntimeException("Could not load module " + configurationBuilder.getModuleName(), e);
        }

        try {
            mongoClientClass = driverClassLoader.loadClass(MONGOCLIENTCLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load " + MONGOCLIENTCLASS, e);
        }

        try {
            mongoClientOptionsClass = driverClassLoader.loadClass(MONGOCLIENTOPTIONSCLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load " + MONGOCLIENTOPTIONSCLASS, e);
        }

        try {
            mongoDatabaseClass = driverClassLoader.loadClass(MONGODATABASECLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load " + MONGODATABASECLASS, e);
        }

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            Method close = mongoClientClass.getMethod("close");
            closeMethod = lookup.unreflect(close);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'close' method " + MONGOCLIENTCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'close' method " + MONGOCLIENTCLASS, e);
        }

        try {
            Method getDatabase = mongoClientClass.getDeclaredMethod("getDatabase", String.class);
            getDatabaseMethod = lookup.unreflect(getDatabase);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'getDatabase' method " + MONGOCLIENTCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'getDatabase' method " + MONGOCLIENTCLASS, e);
        }

        // MongoClient(final List<ServerAddress> seeds, final MongoClientOptions options)
        try {
            Constructor ctor = mongoClientClass.getDeclaredConstructor(List.class, mongoClientOptionsClass);
            mongoClientCtorMethod = lookup.unreflectConstructor(ctor);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'getDatabase' method " + MONGOCLIENTCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'getDatabase' method " + MONGOCLIENTCLASS, e);
        }

        try {
            mongoBuilderClass = driverClassLoader.loadClass(MONGOBUILDERCLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load " + MONGOBUILDERCLASS, e);
        }

        try {
            mongoWriteConcernClass =driverClassLoader.loadClass(MONGOWRITECONCERNCLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load " + MONGOWRITECONCERNCLASS, e);
        }

        try {
            builderCtorMethod = lookup.findConstructor(mongoBuilderClass, MethodType.methodType(void.class));
            // builderCtorMethod.asType((builderCtorMethod.type().changeReturnType(mongoBuilderClass)));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'ctor' method " + MONGOBUILDERCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'ctor'" + MONGOBUILDERCLASS, e);
        }

        try {
            Method method = mongoBuilderClass.getDeclaredMethod("description", String.class);
            descriptionMethod = lookup.unreflect(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'description' method " + MONGOBUILDERCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'description' method" + MONGOBUILDERCLASS, e);
        }

        try {
            Method method = mongoBuilderClass.getMethod("writeConcern", mongoWriteConcernClass);
            writeConcernMethod = lookup.unreflect(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'writeConcern' method " + MONGOBUILDERCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'writeConcern' method" + MONGOBUILDERCLASS, e);
        }

        try {
            Method method = mongoWriteConcernClass.getMethod("valueOf", String.class);
            writeConcernValueOfMethod = lookup.unreflect(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'writeConcern' method " + MONGOBUILDERCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'writeConcern' method" + MONGOBUILDERCLASS, e);
        }

        try {
            Method method = mongoBuilderClass.getMethod("build");
            buildMethod = lookup.unreflect(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'build' method " + MONGOBUILDERCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'build' method" + MONGOBUILDERCLASS, e);
        }

        try {
            mongoServerAddressClass = driverClassLoader.loadClass(MONGOSERVERADDRESSCLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load " + MONGOSERVERADDRESSCLASS, e);
        }

        try {
            serverAddressHostCtor = lookup.findConstructor(mongoServerAddressClass, MethodType.methodType(void.class, String.class));
            serverAddressHostPortCtor = lookup.findConstructor(mongoServerAddressClass, MethodType.methodType(void.class, String.class, int.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get 'ctor' method " + MONGOSERVERADDRESSCLASS, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for 'ctor'" + MONGOSERVERADDRESSCLASS, e);
        }
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
        Object builder = mongoBuilderClass.newInstance();
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
