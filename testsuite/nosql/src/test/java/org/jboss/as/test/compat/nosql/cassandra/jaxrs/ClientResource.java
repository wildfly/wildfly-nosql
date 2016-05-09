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

package org.jboss.as.test.compat.nosql.cassandra.jaxrs;

import java.util.concurrent.ExecutionException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;

/**
 * @author <a href="mailto:kanovotn@redhat.com">Katerina Novotna</a>
 * @author Scott Marlow
 */
@Path("/client")
@Stateless(name = "CustomName")
public class ClientResource {

    // can only use @Resource in EE components, which is why this is a stateless session bean.
    @Resource(lookup = "java:jboss/cassandradriver/test")
    private Cluster cluster;

    private Session session;

    @javax.enterprise.inject.Produces
    @Resource(lookup = "java:jboss/cassandradriver/test")
    private static Cluster injectedCluster;

    @GET
    @Produces({"text/plain"})
    public String get() {
        openConnection();
        try {
            session.execute("CREATE TABLE journal (name varchar primary key, when date, comment varchar)");
            session.execute("INSERT INTO journal (name, when, comment) VALUES " +
                    "('Scott Marlow','2016-05-05','added JAX-RS NoSQL unit test')");
            session.execute("INSERT INTO journal (name, when, comment) VALUES " +
                    "('Scott Marlow','2016-05-05','some NoSQL code cleanup')");
            ResultSetFuture results = session.executeAsync("SELECT JSON * FROM journal");
            return results.get().one().getString("[json]");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while getting results",e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failure while getting results", e);
        } finally {
            closeConnection();
        }

    }

    @POST
    @Produces({"text/plain"})
    public String getInjectedConnection() {
        openInjectedConnection();
        try {
            session.execute("CREATE TABLE journal (name varchar primary key, when date, comment varchar)");
            session.execute("INSERT INTO journal (name, when, comment) VALUES " +
                    "('Scott Marlow','2016-09-05','try injected connection')");
            ResultSetFuture results = session.executeAsync("SELECT JSON * FROM journal");
            return results.get().one().getString("[json]");
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while getting results",e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failure while getting results", e);
        } finally {
            closeConnection();
        }

    }


    private void openConnection() {
        if(cluster == null) {
            throw new RuntimeException("failed to get connection to NoSQL database using @javax.annotation.Resource");
        }
        session = cluster.connect();
        session.execute("CREATE KEYSPACE testspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session = cluster.connect("testspace");
    }


    private void closeConnection() {
        if (session != null) {
            session.execute("DROP KEYSPACE testspace");
            session.close();
            session = null;
        }
    }

    private void openInjectedConnection() {
        if(injectedCluster == null) {
            throw new RuntimeException("failed to get connection to NoSQL database using @javax.enterprise.inject.Produces @Resource");
        }
        session = injectedCluster.connect();
        session.execute("CREATE KEYSPACE testspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session = cluster.connect("testspace");
    }

}
