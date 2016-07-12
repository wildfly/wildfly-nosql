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

    private static final String TRANSACTION_RESOURCE = "_nosqlTXPROXY_";
    private static final String SESSION_RESOURCE = "_nosqlSESSPROXY_";

    SessionProxy(Object session, TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry, String profileName) {
        this.underlyingSession = session;
        this.transactionManager = transactionManager;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
        this.profileName = profileName;
    }

    static Object getSessionFromJTATransaction(TransactionSynchronizationRegistry transactionSynchronizationRegistry, String profileName) {
        return transactionSynchronizationRegistry.getResource(SESSION_RESOURCE + profileName);
    }

    static Object registerSessionWithJTATransaction(Object underlyingSession, TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry, String profileName, String jndiName) {
        SessionProxy sessionProxy = new SessionProxy(underlyingSession, transactionManager, transactionSynchronizationRegistry, profileName);
        Object sessionProxyInstance = Proxy.newProxyInstance(
                underlyingSession.getClass().getClassLoader(),
                underlyingSession.getClass().getInterfaces(),
                sessionProxy
                );
        TransactionProxy transactionProxy = new TransactionProxy();
        Object transactionProxyInstance = Proxy.newProxyInstance(
            // TODO: also load Transaction class from custom user specified modules
            Transaction.class.getClassLoader(),
            new Class<?>[]{Transaction.class, Resource.class, StatementRunner.class},
            transactionProxy);

        TransactionControl transactionControl = sessionProxy.transactionControl(transactionProxy);
        Neo4jXAResourceImpl resource = new Neo4jXAResourceImpl(transactionControl,jndiName, null, null);
        try {
            transactionManager.getTransaction().enlistResource(resource);
        } catch (RollbackException e) {
            throw new RuntimeException(e);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
        transactionSynchronizationRegistry.putResource(TRANSACTION_RESOURCE + profileName,transactionProxyInstance);
        transactionSynchronizationRegistry.putResource(SESSION_RESOURCE + profileName,sessionProxyInstance);
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
                result = transactionSynchronizationRegistry.getResource(TRANSACTION_RESOURCE + profileName);
                if (result != null) {
                    return result;    // return existing transaction proxy enlisted into JTA transaction
                }
                else {
                    throw new RuntimeException("internal error, transaction proxy (" + profileName+ ") not registered with TransactionSynchronizationRegistry");
                }
            } else {
                // For all other javax.transaction.Status states (e.g. STATUS_ROLLEDBACK, ...), fail fast beginTransaction call by throwing an Exception
                throw new RuntimeException("javax.transaction.Status '" + txstatus + "', fail fast the call to '" + method.getName() + "'" );
            }

        }
        else if(method.getName().equals("close")) {
            // ignore call to close, as session/transaction will be auto-closed when JTA transaction ends.
            return null;
        } else {
            // we should have an underlying Neo4j transaction, redirect session invocations to transaction.
            // underlyingTransaction will only be non-null when we have an active JTA transaction.
            if (underlyingTransaction != null) {
                // lookup equivalent method on Transaction class (TODO: cache Transaction methods)
                method = underlyingTransaction.getClass().getMethod(method.getName(),method.getParameterTypes());
                result = method.invoke(underlyingTransaction, args);
            }
            else if(method.getName().equals("isOpen")) {
                // underlyingSession + underlyingTransaction must of been closed, handle isOpen by returning false
                return Boolean.FALSE;
            }
            else {
                // we are only proxying the session if there is an active JTA transaction, so we shouldn't reach the state of
                // no underlyingTransaction.
                // After the underlyingTransaction is closed, no further calls should be made (other than close/isOpen).
                throw new RuntimeException("no underlying Neo4j transaction to invoke '" + method.getName()+"' with.");
            }
        }
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

            /**
             * close the underlying Neo4j Transaction and Neo4j Session when the JTA transaction ends.
             */
            @Override
            public void close() {
                try {
                    underlyingTransaction.getClass().getMethod("close").invoke(underlyingTransaction, null);
                    underlyingTransaction = null;
                    underlyingSession.getClass().getMethod("close").invoke(underlyingSession,null);
                    underlyingSession = null;
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
