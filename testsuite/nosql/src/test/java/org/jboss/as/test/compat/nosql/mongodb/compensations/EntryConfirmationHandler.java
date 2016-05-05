package org.jboss.as.test.compat.nosql.mongodb.compensations;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jboss.narayana.compensations.api.ConfirmationHandler;

import javax.annotation.Resource;
import javax.inject.Inject;

/**
 * If compensating transaction is confirmed, this handler will the new mark entry as confirmed.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class EntryConfirmationHandler implements ConfirmationHandler {

    private static final Document CONFIRM_QUERY = new Document("$set", new Document("confirmed", true));

    @Resource(lookup = EntriesService.DATABASE_JNDI)
    private MongoDatabase mongoDatabase;

    @Inject
    private EntryDocument entryDocument;

    @Override
    public void confirm() {
        mongoDatabase.getCollection(EntriesService.COLLECTION_NAME).updateOne(entryDocument.getDocument(), CONFIRM_QUERY);
    }

}
