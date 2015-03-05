/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.as.nosql.driver.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.jboss.msc.service.StartException;

/**
 * CassandraInteraction is for interacting with Cassandra without static references to Cassandra classes.
 * TODO: switch to MethodHandle.invokeExact(...) for better performance
 *
 * @author Scott Marlow
 */
public class CassandraInteraction {

    private Cluster.Builder clusterBuilder;

    private Cluster.Builder getBuilder() throws StartException {
        if (clusterBuilder == null) {
            this.clusterBuilder = Cluster.builder();
        }
        return clusterBuilder;
    }

    protected Cluster build() throws StartException {
        return getBuilder().build();
    }

    protected Session connect(Cluster cluster, String keySpace) throws StartException {
        return cluster.connect(keySpace);
    }

    protected void withClusterName(String clusterName) throws StartException {
        getBuilder().withClusterName(clusterName);
    }

    protected void withPort(int port) throws StartException {
        getBuilder().withPort(port);
    }

    protected void addContactPoint(String host) throws StartException {
        getBuilder().addContactPoint(host);
    }

    protected void clusterClose(Cluster cluster) throws Throwable {
        cluster.close();
    }

    protected void sessionClose(Session session) throws Throwable {
        session.close();
    }
}
