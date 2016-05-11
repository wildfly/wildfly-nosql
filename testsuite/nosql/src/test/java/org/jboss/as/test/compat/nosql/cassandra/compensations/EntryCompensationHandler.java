package org.jboss.as.test.compat.nosql.cassandra.compensations;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.jboss.narayana.compensations.api.CompensationHandler;

import javax.annotation.Resource;
import javax.inject.Inject;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class EntryCompensationHandler implements CompensationHandler {

    @Inject
    private CompensationScopedEntryContainer entryContainer;

    @Resource(lookup = EntriesService.CLUSTER_JNDI)
    private Cluster cluster;

    @Override
    public void compensate() {
        Session session = cluster.connect(EntriesService.KEYSPACE_NAME);
        session.execute(
                "DELETE FROM " + EntriesService.TABLE_NAME + " WHERE entry='" + entryContainer.getEntry().getValue() + "'");
        session.close();
    }

}
