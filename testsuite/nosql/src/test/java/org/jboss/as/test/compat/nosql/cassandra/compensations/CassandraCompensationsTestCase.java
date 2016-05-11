package org.jboss.as.test.compat.nosql.cassandra.compensations;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.impl.BAControllerFactory;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class CassandraCompensationsTestCase {

    private static final String ARCHIVE_NAME = CassandraCompensationsTestCase.class.getSimpleName();

    @Resource(lookup = EntriesService.CLUSTER_JNDI)
    private Cluster cluster;

    private Session session;

    @Inject
    private EntriesService entriesService;

    @Deployment
    public static JavaArchive deploy() {
        return ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME + ".jar")
                .addPackage(CassandraCompensationsTestCase.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void before() {
        session = cluster.connect();
        session.execute("CREATE KEYSPACE IF NOT EXISTS " + EntriesService.KEYSPACE_NAME
                + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.close();
        session = cluster.connect(EntriesService.KEYSPACE_NAME);
        session.execute("CREATE TABLE " + EntriesService.TABLE_NAME + " (entry varchar PRIMARY KEY, confirmed boolean)");
    }

    @After
    public void after() {
        session.execute("DROP KEYSPACE IF EXISTS " + EntriesService.KEYSPACE_NAME);
        session.close();
    }

    @Test
    public void shouldAddEntryToTheDatabaseAndConfirmTransaction() throws Exception {
        Entry entry = getEntry();
        BAControllerFactory.getInstance().beginBusinessActivity();
        entriesService.insert(entry);
        assertSingleEntry(entry); // Transaction is not yet completed. Entry should exist, but shouldn't be confirmed.
        BAControllerFactory.getInstance().closeBusinessActivity();
        assertSingleEntry(new Entry(entry.getValue(), true)); // Transaction was completed. A confirmed entry should exist.
    }

    @Test
    public void shouldAddEntryToTheDatabaseAndCancelTransaction() throws Exception {
        Entry entry = getEntry();
        BAControllerFactory.getInstance().beginBusinessActivity();
        entriesService.insert(entry);
        assertSingleEntry(entry); // Transaction is not yet completed. Entry should exist, but shouldn't be confirmed.
        BAControllerFactory.getInstance().cancelBusinessActivity();
        assertEquals("Unexpected entries", 0, entriesService.getAll().size()); // Transaction was canceled. Entry shouldn't exist.
    }

    private void assertSingleEntry(Entry entry) {
        List<Entry> entries = entriesService.getAll();
        assertEquals("One entry is expected.", 1, entries.size());
        assertEquals("Wrong entry found.", entry, entries.get(0));
    }

    private Entry getEntry() {
        return new Entry("TestEntry at " + LocalTime.now(), false);
    }

}
