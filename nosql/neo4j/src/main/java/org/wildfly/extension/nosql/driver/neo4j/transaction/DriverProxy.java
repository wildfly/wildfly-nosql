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

package org.wildfly.extension.nosql.driver.neo4j.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * DriverProxy
 *
 * @author Scott Marlow
 */
public class DriverProxy implements InvocationHandler {
    private Object underlyingDriver;
    private TransactionManager transactionManager;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    private String profileName;
    private String jndiName;

    public DriverProxy(Object driver, TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry, String profileName, String jndiName) {
        this.underlyingDriver = driver;
        this.transactionManager = transactionManager;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
        this.profileName = profileName;
        this.jndiName = jndiName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result = method.invoke(underlyingDriver, args);
        if (method.getName().equals("session")) {  // wrap returned Session instance
            return SessionProxy.sessionProxy(result, transactionManager, transactionSynchronizationRegistry, profileName, jndiName);
        }
        return result;
    }
}

