package org.jboss.as.test.compat.nosql.cassandra.compensations;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.jboss.narayana.compensations.api.ConfirmationHandler;

import javax.annotation.Resource;
import javax.inject.Inject;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class EntryConfirmationHandler implements ConfirmationHandler {

    @Inject
    private CompensationScopedEntryContainer entryContainer;

    @Resource(lookup = EntriesService.CLUSTER_JNDI)
    private Cluster cluster;

    @Override
    public void confirm() {
        Session session = cluster.connect(EntriesService.KEYSPACE_NAME);
        session.execute("UPDATE " + EntriesService.TABLE_NAME + " SET confirmed=true WHERE entry='"
                + entryContainer.getEntry().getValue() + "'");
        session.close();
    }

}
