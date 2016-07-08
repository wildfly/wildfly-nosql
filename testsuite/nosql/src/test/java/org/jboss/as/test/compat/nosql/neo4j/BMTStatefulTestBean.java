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

package org.jboss.as.test.compat.nosql.neo4j;

import javax.annotation.Resource;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.UserTransaction;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

/**
 * test with bean managed transactions
 *
 * @author Scott Marlow
 */
@Stateful
@TransactionManagement(TransactionManagementType.BEAN)
public class BMTStatefulTestBean {

    @Resource
    private UserTransaction userTransaction;

    @Resource(lookup = "java:jboss/neo4jdriver/test")
    private Driver driver;

    public String twoTransactions() throws Exception {
        // obtain session without an active JTA transaction, session will not be enlisted into a JTA transaction yet.
        // if driver.session was called after the JTA transaction is started, session would of been enlisted into the
        // JTA transaction.
        Session session = driver.session();
        // start the JTA transaction via javax.transaction.UserTransaction
        userTransaction.begin();
        // obtain org.neo4j.driver.v1.Transaction within JTA transaction, which will be enlisted into the JTA transaction.
        // if session.beginTransaction() was called before the JTA transaction started, the org.neo4j.driver.v1.Transaction
        // wouldn't be enlisted into JTA transaction.
        Transaction transaction = session.beginTransaction();
        try {
            transaction.run("CREATE (a:Person {name:'BMT', title:'King'})");
            // the following two calls (tx.success()/tx.close()) are ignored, instead when the JTA transaction ends, the following two calls are
            // then made internally.
            transaction.success();
            transaction.close();
            // commit the JTA transaction, which also calls org.neo4j.driver.v1.Transaction.success()/close().
            // if the JTA transaction rolls back, org.neo4j.driver.v1.Transaction.failure()/close() would instead be called.
            userTransaction.commit();

            // Start another JTA transaction, note that the same Session is still used, since it is still open.
            // TODO: Consider design change to have enlisted org.neo4j.driver.v1.Session, be auto closed when JTA transaction ends,
            //       which would require this test app to call driver.session() again.
            userTransaction.begin();
            transaction = session.beginTransaction();

            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = 'BMT' RETURN a.name AS name, a.title AS title");
            Record record = result.next();
            return record.toString();
        } finally {
            if ( transaction.isOpen()) {  // this should return true, as the JTA transaction is still active
                session.run("MATCH (a:Person) delete a");
                transaction.close();      // this call to close, should be ignored, as the transaction is still active.
            }
            session.close();              // TODO: see above TODO about auto-closing session at transaction end, if we did
                                          //       that, we would have to ignore this call to session.close().
            userTransaction.commit();     // second JTA transaction is ended, which also closes the org.neo4j.driver.v1.Transaction/Session
        }
    }



}
