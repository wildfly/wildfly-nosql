package org.jboss.as.test.compat.nosql.mongodb.compensations;

import org.bson.Document;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.impl.BAControllerFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class MongoDBCompensationsTestCase {

    private static final String ARCHIVE_NAME = MongoDBCompensationsTestCase.class.getSimpleName() + "_test";

    @Inject
    private EntriesService entriesService;

    @Deployment
    public static Archive<?> deploy() throws Exception {
        return ShrinkWrap.create(JavaArchive.class, ARCHIVE_NAME + ".jar")
                .addPackage(MongoDBCompensationsTestCase.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @After
    public void after() {
        entriesService.cleanup();
    }

    @Test
    public void shouldAddEntryToTheDatabaseAndConfirmTransaction() throws Exception {
        String entry = getEntry();
        BAControllerFactory.getInstance().beginBusinessActivity();
        entriesService.insert(entry);
        assertSingleEntry(entry, false); // Transaction is not yet completed. Entry should exist, but shouldn't be confirmed.
        BAControllerFactory.getInstance().closeBusinessActivity();
        assertSingleEntry(entry, true); // Transaction was completed. A confirmed entry should exist.
    }

    @Test
    public void shouldAddEntryToTheDatabaseAndCancelTransaction() throws Exception {
        String entry = getEntry();
        BAControllerFactory.getInstance().beginBusinessActivity();
        entriesService.insert(entry);
        assertSingleEntry(entry, false); // Transaction is not yet completed. Entry should exist, but shouldn't be confirmed.
        BAControllerFactory.getInstance().cancelBusinessActivity();
        assertEquals("Unexpected entries", 0, entriesService.getAll().size()); // Transaction was canceled. Entry shouldn't exist.
    }

    private void assertSingleEntry(String entry, boolean isConfirmed) {
        List<Document> documents = entriesService.getAll();
        assertEquals("One entry is expected.", 1, documents.size());
        assertEquals("Wrong entry.", entry, documents.get(0).getString("entry"));
        assertEquals("Wrong confirmation status.", isConfirmed, documents.get(0).getBoolean("confirmed"));
    }

    private String getEntry() {
        return "TestEntry at " + LocalTime.now();
    }

}
