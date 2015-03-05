package org.jboss.as.test.compat.nosql.cassandra;

import javax.annotation.Resource;
import javax.ejb.Stateful;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * StatefulTestBean
 *
 * @author Scott Marlow
 */
@Stateful
public class StatefulTestBean {

    @Resource(lookup = "java:jboss/cassandradriver/test")
    private Cluster cluster;
    private Session session;

    public void query() {
        openConnection();
        try {
            session.execute("CREATE TABLE employee (lastname varchar primary key, firstname varchar, age int, city varchar, email varchar)");
            session.execute("INSERT INTO employee (lastname, firstname, age, city, email) VALUES ('Smith','Leanne', 30, 'Boston', 'lea@yahoo.com')");
            ResultSet results = session.execute("SELECT * FROM employee WHERE lastname='Smith'");
            for (Row row : results) {
                System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
            }
            session.execute("update employee set age = 36 where lastname = 'Smith'");
            // Select and show the change
            results = session.execute("select * from employee where lastname='Smith'");
            for (Row row : results) {
                System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
            }
        } finally {
            try {
                session.execute("DROP TABLE employee");
            } catch (Throwable ignore) {}

            closeConnection();
        }
    }

    public void asyncQuery() {
        openConnection();
        try {
            session.execute("CREATE TABLE employee (lastname varchar primary key, firstname varchar, age int, city varchar, email varchar)");
            session.execute("INSERT INTO employee (lastname, firstname, age, city, email) VALUES ('Smith','Leanne', 30, 'Boston', 'lea@yahoo.com')");
            ResultSetFuture results = session.executeAsync("SELECT * FROM employee WHERE lastname='Smith'");

            for (Row row : results.getUninterruptibly()) {
                System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
            }
            session.execute("update employee set age = 36 where lastname = 'Smith'");
            // Select and show the change
            results = session.executeAsync("select * from employee where lastname='Smith'");
            for (Row row : results.getUninterruptibly()) {
                System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
            }
        } finally {
            try {
                session.execute("DROP TABLE employee");
            } catch (Throwable ignore) {}

            closeConnection();
        }
    }


    private void openConnection() {
        session = cluster.connect();
        session.execute("CREATE KEYSPACE testspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session = cluster.connect("testspace");
    }


    private void closeConnection() {
        session.execute("DROP KEYSPACE testspace");
        session.close();
    }

}
