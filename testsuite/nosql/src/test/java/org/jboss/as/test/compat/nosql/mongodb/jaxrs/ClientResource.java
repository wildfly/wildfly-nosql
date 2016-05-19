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

package org.jboss.as.test.compat.nosql.mongodb.jaxrs;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.wildfly.extension.nosql.cdi.Mongo;

/**
 * @author <a href="mailto:kanovotn@redhat.com">Katerina Novotna</a>
 * @author Scott Marlow
 */
@Path("/client")
@Stateless(name = "CustomName")
public class ClientResource {

    @Inject
    // @Mongo(profile="mongodbtestprofile")
    MongoClient connection;

    @Inject
    // @Mongo(profile="mongodbtestprofile")
    MongoDatabase database;

    // can only use @Resource in EE components, which is why this is a stateless session bean.
    @Resource(lookup = "java:jboss/mongodb/test")
    MongoDatabase other;

    @GET
    @Produces({"text/plain"})
    public String get() {

        MongoCollection collection = null;
        Document query = null;
        try {
            // collection = database.getDatabase("mongotestdb").getCollection("company");
            collection = database.getCollection("company");
            String companyName = "Acme products";
            JsonObject object = Json.createObjectBuilder()
                    .add("companyName", companyName)
                    .add("street", "999 Flow Lane")
                    .add("city", "Indiville")
                    .add("_id", companyName)
                    .build();
            Document document = Document.parse(object.toString());
            collection.insertOne(document);
            query = new Document("_id", companyName);
            FindIterable cursor = collection.find(query);
            Object dbObject = cursor.first();
            return dbObject.toString();
        } finally {
            if (query != null) {
                collection.drop();
            }
        }
    }
}
