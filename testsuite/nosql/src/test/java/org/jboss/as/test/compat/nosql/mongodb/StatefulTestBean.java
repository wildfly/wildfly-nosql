package org.jboss.as.test.compat.nosql.mongodb;

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.json.Json;
import javax.json.JsonObject;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * StatefulTestBean for the MongoDB document database
 *
 * @author Scott Marlow
 */
@Stateful
public class StatefulTestBean {

    @Resource(lookup = "java:jboss/mongodb/test")
    MongoDatabase database;

    public void addUserComment() {
        MongoCollection collection = null;
        Document query = null;
        try {
            // add a comment from user Melanie
            String who = "Melanie";
            Document comment = new Document("_id", who)
                    .append("name", who)
                    .append("address", new BasicDBObject("street", "123 Main Street")
                            .append("city", "Fastville")
                            .append("state", "MA")
                            .append("zip", 18180))
                    .append("comment","I really love your new website but I have a lot of questions about using NoSQL versus a traditional RDBMS.  "+
                        "I would like to sign up for your 'Mongo DB Is Web Scale' training session.");
            // save the comment
            collection = database.getCollection("comments");
            collection.insertOne(comment);

            // look up the comment from Melanie
            query = new Document("_id", who);
            FindIterable cursor = collection.find(query);
            Object userComment = cursor.first();
            System.out.println("DBObject.toString() = " + userComment.toString());
        } finally {
            collection.drop();
        }
    }

    public void addProduct() {
        MongoCollection collection = null;
        Document query = null;
        try {
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
            query = new Document("_id",companyName);
            FindIterable cursor = collection.find(query);
            Object dbObject = cursor.first();
            System.out.println("DBObject.toString() = " + dbObject.toString());
        } finally {
            if (query != null) {
                collection.drop();
            }
        }
    }
}
