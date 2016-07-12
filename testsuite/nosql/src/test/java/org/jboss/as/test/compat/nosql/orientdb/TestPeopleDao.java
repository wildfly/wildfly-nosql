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
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class TestPeopleDao {

    private final OPartitionedDatabasePool databasePool;

    public TestPeopleDao(OPartitionedDatabasePool databasePool) {
        this.databasePool = databasePool;
    }

    public ODocument addPerson(String name) {
        try (ODatabaseDocumentTx database = databasePool.acquire()) {
            ODocument document = new ODocument("Person");
            document.field("name", name);
            database.commit();
            return document.save();
        }
    }

    public List<ODocument> getPeople() {
        try (ODatabaseDocumentTx database = databasePool.acquire()) {
            OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("SELECT * FROM Person");
            return database.command(query).execute();
        }
    }

    public OrientEdge addFriend(String outName, String inName) {
        OrientGraph database = new OrientGraph(databasePool);
        try {
            Vertex outVertex = database.addVertex(null);
            Vertex inVertex = database.addVertex(null);
            outVertex.setProperty("name", outName);
            inVertex.setProperty("name", inName);
            OrientEdge edge = database.addEdge(null, outVertex, inVertex, "knows");
            database.commit();
            return edge;
        } catch (Exception e) {
            database.rollback();
        } finally {
            database.shutdown();
        }

        return null;
    }

    public List<Edge> getFriends() {
        List<Edge> edges = new LinkedList<>();
        OrientGraph database = new OrientGraph(databasePool);
        try {
            database.getEdges().forEach(edges::add);
        } finally {
            database.shutdown();
        }

        return edges;
    }

}
