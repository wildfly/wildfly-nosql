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
package org.wildfly.nosql.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
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
}
