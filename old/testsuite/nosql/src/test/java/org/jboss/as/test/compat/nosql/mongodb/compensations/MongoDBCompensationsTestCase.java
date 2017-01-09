package org.jboss.as.test.compat.nosql.mongodb.compensations;

import org.bson.Document;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class MongoDBCompensationsTestCase {

    @Inject
    private AccountDao accountDao;

    @Inject
    private BankingService bankingService;

    @Deployment
    public static Archive<?> deploy() throws Exception {
        return ShrinkWrap.create(JavaArchive.class, MongoDBCompensationsTestCase.class.getSimpleName() + ".jar")
                .addPackage(MongoDBCompensationsTestCase.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void before() {
        accountDao.insert(new Document("name", "A").append("balance", 1000));
        accountDao.insert(new Document("name", "B").append("balance", 1000));
    }

    @After
    public void after() {
        accountDao.clear();
    }

    @Test
    public void testSuccess() {
        bankingService.transferMoney("A", "B", 100);
        assertBalance("A", 900);
        assertBalance("B", 1100);
    }

    @Test
    public void testFailure() {
        try {
            bankingService.transferMoney("A", "B", 600);
        } catch (TransactionCompensatedException ignored) {
        }
        assertBalance("A", 1000);
        assertBalance("B", 1000);
    }

    private void assertBalance(String accountName, int expectedBalance) {
        Optional<Document> account = accountDao.get(accountName);
        assertTrue(account.isPresent());
        assertEquals(expectedBalance, account.get().get("balance"));
    }

}
