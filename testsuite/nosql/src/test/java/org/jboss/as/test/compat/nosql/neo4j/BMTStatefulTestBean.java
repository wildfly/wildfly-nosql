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
        userTransaction.begin();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        try {
            transaction.run("CREATE (a:Person {name:'BMT', title:'King'})");
            transaction.success();
            transaction.close();

            userTransaction.commit();
            userTransaction.begin();
            transaction = session.beginTransaction();

            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = 'BMT' RETURN a.name AS name, a.title AS title");
            Record record = result.next();
            return record.toString();
        } finally {
            if ( transaction.isOpen()) {
                session.run("MATCH (a:Person) delete a");
                transaction.close();
            }
            session.close();
            userTransaction.commit();
        }
    }



}
