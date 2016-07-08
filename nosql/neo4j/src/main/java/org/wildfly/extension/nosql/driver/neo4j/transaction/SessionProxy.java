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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.neo4j.driver.v1.StatementRunner;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.util.Resource;

/**
 * SessionProxy
 *
 * @author Scott Marlow
 */
public class SessionProxy implements InvocationHandler {

    private Object underlyingSession;
    private Object underlyingTransaction;
    private TransactionManager transactionManager;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;
    private String profileName;
    private String jndiName;

    SessionProxy(Object session, TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry, String profileName, String jndiName) {
        this.underlyingSession = session;
        this.transactionManager = transactionManager;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
        this.profileName = profileName;
        this.jndiName = jndiName;

    }

    static Object sessionProxy(Object underlyingSession, TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry, String profileName, String jndiName) {
        // TODO: change to session per JTA transaction, where session is autoclosed at JTA transaction end
        //       which means that multiple calls to Driver.session, within the same JTA transaction, would return same session.
        // TODO: also, instead of creating a new SessionProxy for every call to Driver.session(), we
        //       should cache the SessionProxy per JTA transaction as well.
        SessionProxy sessionProxy = new SessionProxy(underlyingSession, transactionManager, transactionSynchronizationRegistry, profileName, jndiName);
        Object sessionProxyInstance = Proxy.newProxyInstance(
                underlyingSession.getClass().getClassLoader(),
                underlyingSession.getClass().getInterfaces(),
                sessionProxy
                );
        try {
            int txstatus = transactionManager.getStatus();
            // if jta transaction is active,
            if (txstatus == Status.STATUS_ACTIVE || txstatus == Status.STATUS_MARKED_ROLLBACK) {
                Object transactionProxy = transactionSynchronizationRegistry.getResource(profileName);
                if (transactionProxy == null) {
                    sessionProxy.transactionProxy();  // register TransactionProxy
                }
            }
        } catch (SystemException e) {

        }

        return sessionProxyInstance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        if (method.getName().equals("beginTransaction")) {  /// Transaction beginTransaction();
            int txstatus = transactionManager.getStatus();
            // if jta transaction is active,
            if (txstatus == Status.STATUS_ACTIVE || txstatus == Status.STATUS_MARKED_ROLLBACK) {
                //   return existing XAResource associated with transaction, using profile name of NoSQL connection
                result = transactionSynchronizationRegistry.getResource(profileName);
                if (result != null) {
                    return result;    // return existing TransactionProxy
                }
                else {
                    return transactionProxy();  // return newly registered TransactionProxy
                }
            } else {
                // no active transaction, delegate beginTransaction call to underlying session
                result = method.invoke(underlyingSession, args);
            }

        } else {
            // if we have an active JTA transaction, redirect session invocations to transaction.
            // underlyingTransaction will only be non-null when we have an active JTA transaction.
            if (underlyingTransaction != null) {
                // lookup equivalent method on Transaction class (TODO: cache methods)
                method = underlyingTransaction.getClass().getMethod(method.getName(),method.getParameterTypes());
                result = method.invoke(underlyingTransaction, args);
            }
            else {
                result = method.invoke(underlyingSession, args);
            }
        }
        return result;
    }

    private Object transactionProxy() {
        TransactionProxy transactionProxy = new TransactionProxy();
        Object result = Proxy.newProxyInstance(
                            Transaction.class.getClassLoader(), // Todo: also load Transaction class from custom user specified modules
                            new Class<?>[]{Transaction.class, Resource.class, StatementRunner.class},
                            transactionProxy);

        TransactionControl transactionControl = transactionControl(transactionProxy);
        Neo4jXAResourceImpl resource = new Neo4jXAResourceImpl(transactionControl,jndiName, null, null);
        try {
            transactionManager.getTransaction().enlistResource(resource);
        } catch (RollbackException e) {
            e.printStackTrace();
        } catch (SystemException e) {
            e.printStackTrace();
        }
        transactionSynchronizationRegistry.putResource(profileName,result);
        return result;
    }

    private TransactionControl transactionControl(final TransactionProxy transactionProxy) {

        return new TransactionControl() {

            @Override
            public Object beginTransaction() {
                try {
                    underlyingTransaction = underlyingSession.getClass().getMethod("beginTransaction").invoke(underlyingSession, null);
                    transactionProxy.setUnderlyingTransaction(underlyingTransaction);
                    return underlyingTransaction;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("could not begin transaction", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("could not begin transaction", e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("could not begin transaction", e);
                }
            }

            @Override
            public void success() {
                try {
                    underlyingTransaction.getClass().getMethod("success").invoke(underlyingTransaction, null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("could not mark transaction as successful", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("could not mark transaction as successful", e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("could not mark transaction as successful", e);
                }
            }

            @Override
            public void failure() {
                try {
                    underlyingTransaction.getClass().getMethod("failure").invoke(underlyingTransaction, null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("could not mark transaction as failed", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("could not mark transaction as failed", e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("could not mark transaction as failed", e);
                }
            }

            @Override
            public void close() {
                try {
                    underlyingTransaction.getClass().getMethod("close").invoke(underlyingTransaction, null);
                    underlyingTransaction = null;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("could not close the transaction", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("could not close the transaction", e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("could not close the transaction", e);
                }

            }
        };
    }

}
