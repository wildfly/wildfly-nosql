package org.jboss.as.test.compat.nosql.mongodb.compensations;

import com.mongodb.client.MongoDatabase;
import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.annotation.Resource;
import javax.inject.Inject;

/**
 * If compensating transaction is canceled, this handler will remove the new entry.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class EntryCompensationHandler implements CompensationHandler {

    @Resource(lookup = EntriesService.DATABASE_JNDI)
    private MongoDatabase mongoDatabase;

    @Inject
    private EntryDocument entryDocument;

    @Override
    public void compensate() {
        mongoDatabase.getCollection(EntriesService.COLLECTION_NAME).deleteOne(entryDocument.getDocument());
    }

}
