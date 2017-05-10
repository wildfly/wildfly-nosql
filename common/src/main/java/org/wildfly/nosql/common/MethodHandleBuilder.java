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

package org.wildfly.nosql.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * MethodHandleBuilder helps construct a series of MethodHandle's.
 *
 * Usage:
 * MethodHandleBuilder mhb = new MethodHandleBuilder();
 * mhb.classLoader(moduleIdentifier).className("com.mongodb.MongoClient")
 * MethodHandle closeMethod = mhb.method("close");
 * mhb.className("otherClassName");
 * MethodHandle otherMethod = mhb.method("otherMethod", Integer.class);
 *
 * @author Scott Marlow
 */
public class MethodHandleBuilder {

    private ClassLoader classLoader; // caller can switch to a new classloader at any time, which impacts the next call to MethodHandleBuilder
    private Class targetClass;  // caller can switch to a new targetClass at any time, which impacts the next call to MethodHandleBuilder

    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public MethodHandleBuilder classLoader(ModuleIdentifier moduleIdentifier) {
        Module module;
        try {
            module = Module.getBootModuleLoader().loadModule(moduleIdentifier);
        } catch (ModuleLoadException e) {
            // TODO: use NoSQLLogger for all exceptions created here
            throw new RuntimeException("Could not load module " + moduleIdentifier.getName(), e);
        }
        this.classLoader = module.getClassLoader();
        return this;
    }

    public MethodHandleBuilder className(String className) {
        if (classLoader == null) {
            throw new IllegalStateException("ClassLoader needs to be specified");
        }
        try {
            targetClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load " + className, e);
        }
        return this;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public MethodHandle method(String methodName, Class<?>... parameterTypes) {
        try {
            Method method = targetClass.getMethod(methodName, parameterTypes);
            return lookup.unreflect(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get '"+methodName+"' method from " + targetClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for '"+methodName+"' method from " + targetClass.getName(), e);
        }
    }

    public MethodHandle declaredMethod(String methodName, Class<?>... parameterTypes) {
        try {
            Method method = targetClass.getDeclaredMethod(methodName, parameterTypes);
            return lookup.unreflect(method);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get '"+methodName+"' method from " + targetClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get MethodHandle for '"+methodName+"' method from " + targetClass.getName(), e);
        }
    }


    public MethodHandle declaredConstructor(Class<?>... parameterTypes) {
        try {
            Constructor ctor = targetClass.getDeclaredConstructor(parameterTypes);
            return lookup.unreflectConstructor(ctor);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get constructor for " + targetClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get constructor MethodHandle for " + targetClass.getName(), e);
        }
    }

    public MethodHandle constructor(MethodType methodType) {
        try {
             return lookup.findConstructor(targetClass, methodType);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get constructor for " + targetClass.getName() + " with MethodType " + methodType.toString(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get constructor MethodHandle for " + targetClass.getName(), e);
        }
    }

    public MethodHandle staticField(String name) {
        try {
            Field field = targetClass.getField(name);
            return lookup.unreflectGetter(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not get static field " + name + " on class " + targetClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get static field " + name + " on class " + targetClass.getName(), e);
        }
    }

    public MethodHandle staticMethod(String name, MethodType methodType) {
        try {
            return lookup.unreflect(targetClass.getMethod(name, methodType.parameterArray()));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not get static method " + name + " on class " + targetClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get static method " + name + " on class " + targetClass.getName(), e);
        }
    }

}
