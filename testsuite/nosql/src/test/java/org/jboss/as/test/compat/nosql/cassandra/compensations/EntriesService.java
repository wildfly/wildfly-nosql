package org.jboss.as.test.compat.nosql.cassandra.compensations;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.inject.Inject;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.jboss.narayana.compensations.api.Compensatable;
import org.jboss.narayana.compensations.api.TxCompensate;
import org.jboss.narayana.compensations.api.TxConfirm;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Compensatable
@Stateful
public class EntriesService {

    public static final String CLUSTER_JNDI = "java:jboss/cassandradriver/test";

    public static final String KEYSPACE_NAME = "testspace";

    public static final String TABLE_NAME = "entriestable";

    @Inject
    private CompensationScopedEntryContainer entryContainer;

    @Resource(lookup = CLUSTER_JNDI)
    private Cluster cluster;

    private Session session;

    @TxConfirm(EntryConfirmationHandler.class)
    @TxCompensate(EntryCompensationHandler.class)
    public void insert(Entry entry) {
        connect();
        entryContainer.setEntry(entry);
        session.execute("INSERT INTO " + TABLE_NAME + " (entry, confirmed) VALUES ('" + entry.getValue() + "', "
                + entry.isConfirmed() + ")");
        disconnect();
    }

    public List<Entry> getAll() {
        connect();
        List<Entry> entries = new LinkedList<>();
        session.executeAsync("SELECT * FROM " + TABLE_NAME).getUninterruptibly().iterator()
                .forEachRemaining(r -> entries.add(new Entry(r.getString(0), r.getBool(1))));
        disconnect();
        return entries;
    }

    private void connect() {
        assert session == null : "Session is already connected";
        session = cluster.connect(KEYSPACE_NAME);
    }

    private void disconnect() {
        assert session != null : "Session has to be connected";
        session.close();
        session = null;
    }

}
