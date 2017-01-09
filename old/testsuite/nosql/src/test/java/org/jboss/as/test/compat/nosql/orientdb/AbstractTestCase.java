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

package org.jboss.as.test.compat.nosql.orientdb;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class AbstractTestCase {

    private static final String DATABASE_JNDI = "java:jboss/orientdb/test";

    @ArquillianResource
    private static InitialContext initialContext;

    @Before
    public void before() throws NamingException {
        try {
            clearDatabase();
        } catch (Throwable t) {
            // Database might not exist at this stage
        }
        initDatabase();
    }

    @After
    public void after() throws NamingException {
        clearDatabase();
    }

    private OPartitionedDatabasePool getDatabasePool() throws NamingException {
        return (OPartitionedDatabasePool) initialContext.lookup(DATABASE_JNDI);
    }

    private void initDatabase() throws NamingException {
        try (ODatabaseDocumentTx database = getDatabasePool().acquire()) {
            database.getMetadata().getSchema().createClass("Person").createProperty("name", OType.STRING);
        }
    }

    private void clearDatabase() throws NamingException {
        try (ODatabaseDocumentTx database = getDatabasePool().acquire()) {
            database.getMetadata().getSchema().dropClass("Person");
        }

        OrientGraph database = new OrientGraph(getDatabasePool());
        try {
            database.getEdges().forEach(database::removeEdge);
            database.getVertices().forEach(database::removeVertex);
        } finally {
            database.shutdown();
        }
    }

}
