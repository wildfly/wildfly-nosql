package org.jboss.as.test.compat.nosql.mongodb.compensations;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Compensatable
@Stateful
public class EntriesService {

    public static final String DATABASE_JNDI = "java:jboss/mongodb/test";

    public static final String COLLECTION_NAME = "compensatable-service-entries";

    @Resource(lookup = DATABASE_JNDI)
    private MongoDatabase database;

    @Inject
    private EntryDocument entryDocument;

    @TxConfirm(EntryConfirmationHandler.class)
    @TxCompensate(EntryCompensationHandler.class)
    public void insert(String entry) {
        Document document = new Document("entry", entry).append("confirmed", false);
        getCollection().insertOne(document);

        // Saving document in a compensation scoped object in order to make it accessible for handlers
        entryDocument.setDocument(document);
    }

    public List<Document> getAll() {
        List<Document> documents = new LinkedList<>();
        getCollection().find().iterator().forEachRemaining(documents::add);

        return documents;
    }

    public void cleanup() {
        getCollection().drop();
    }

    private MongoCollection<Document> getCollection() {
        return database.getCollection(COLLECTION_NAME);
    }

}
