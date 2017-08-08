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

package org.wildfly.extension.nosql.driver.neo4j.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

/**
 * DriverProxy creates SessionProxy if JTA transaction is active during Driver.session() call.
 *
 * @author Scott Marlow
 */
public class DriverProxy implements InvocationHandler {
    private final Object underlyingDriver;
    private final TransactionManager transactionManager;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    private final String profileName;
    private final String jndiName;

    public DriverProxy(Object driver, TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry, String profileName, String jndiName) {
        this.underlyingDriver = driver;
        this.transactionManager = transactionManager;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
        this.profileName = profileName;
        this.jndiName = jndiName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        if (method.getName().equals("session")) {  // wrap returned Session instance
            // if jta transaction is active,
            int txstatus = transactionManager.getStatus();
            if (txstatus == Status.STATUS_ACTIVE || txstatus == Status.STATUS_MARKED_ROLLBACK) {
                Object result = SessionProxy.getSessionFromJTATransaction(transactionSynchronizationRegistry, profileName);
                if ( result != null) {  // return existing JTA active session
                    return result;
                }
                return SessionProxy.registerSessionWithJTATransaction(
                        method.invoke(underlyingDriver, args),      // pass underlying driver
                        transactionManager,
                        transactionSynchronizationRegistry,
                        profileName,
                        jndiName);
            }
        }
        Object result = method.invoke(underlyingDriver, args);
        return result;
    }
}

