package org.jboss.as.test.compat.nosql.cassandra.compensations;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.narayana.compensations.api.TransactionCompensatedException;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author <a href="mailto:paul.robinson@redhat.com">Paul Robinson</a>
 */
@RunWith(Arquillian.class)
public class CassandraCompensationsTestCase {

    @Inject
    private BankingService bankingService;

    @Inject
    private BalanceDao balanceDao;

    @Deployment
    public static JavaArchive deploy() {
        return ShrinkWrap.create(JavaArchive.class, CassandraCompensationsTestCase.class.getSimpleName() + ".jar")
                .addPackage(CassandraCompensationsTestCase.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void before() {
        balanceDao.init();
    }

    @After
    public void after() {
        balanceDao.clear();
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

    private void assertBalance(String account, int expectedBalance) {
        assertEquals(expectedBalance, balanceDao.get(account));
    }

}
