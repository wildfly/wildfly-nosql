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

package org.jboss.as.test.compat.nosql.cassandra.compensations;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

import javax.annotation.Resource;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class BalanceDao {

    private static final String KEYSPACE_NAME = "accounts";

    private static final String TABLE_NAME = "accounts";

    private static final String CREATE_KEYSPACE_QUERY = "CREATE KEYSPACE IF NOT EXISTS " + KEYSPACE_NAME
            + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + " (name text PRIMARY KEY, balance int)";

    private static final String CLEAR_QUERY = "DROP KEYSPACE IF EXISTS " + KEYSPACE_NAME;

    private static final String INSERT_QUERY = "INSERT INTO " + TABLE_NAME + " (name, balance) VALUES ('%s', %d)";

    private static final String UPDATE_QUERY = "UPDATE " + TABLE_NAME + " SET balance=%d WHERE name='%s'";

    private static final String SELECT_QUERY = "SELECT balance FROM " + TABLE_NAME + " WHERE name='%s'";

    @Resource(lookup = "java:jboss/cassandradriver/test")
    private Cluster cluster;

    public void init() {
        Session session = cluster.connect();
        session.execute(CREATE_KEYSPACE_QUERY);
        session.close();

        session = cluster.connect(KEYSPACE_NAME);
        session.execute(CREATE_TABLE_QUERY);
        session.execute(String.format(INSERT_QUERY, "A", 1000));
        session.execute(String.format(INSERT_QUERY, "B", 1000));
        session.close();
    }

    public int get(String accountName) {
        Session session = cluster.connect(KEYSPACE_NAME);
        ResultSet result = session.execute(String.format(SELECT_QUERY, accountName));
        session.close();
        assert result != null;
        return result.one().getInt(0);
    }

    public void update(String accountName, int balance) {
        Session session = cluster.connect(KEYSPACE_NAME);
        session.execute(String.format(UPDATE_QUERY, balance, accountName));
        session.close();
    }

    public void clear() {
        Session session = cluster.connect(KEYSPACE_NAME);
        session.execute(CLEAR_QUERY);
        session.close();
    }

}
