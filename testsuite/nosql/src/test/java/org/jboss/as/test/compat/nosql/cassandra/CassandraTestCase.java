package org.jboss.as.test.compat.nosql.cassandra;

import static org.junit.Assert.assertTrue;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.datastax.driver.core.Cluster;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * CassandraTestCase
 *
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
public class CassandraTestCase {
    private static final String ARCHIVE_NAME = "CassandraTestCase_test";

@ArquillianResource
   private static InitialContext iniCtx;

   @BeforeClass
   public static void beforeClass() throws NamingException {
       iniCtx = new InitialContext();
   }

   @Deployment
   public static Archive<?> deploy() throws Exception {

       EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, ARCHIVE_NAME + ".ear");
       JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "beans.jar");
       lib.addClasses(StatefulTestBean.class);
       ear.addAsModule(lib);
       final WebArchive main = ShrinkWrap.create(WebArchive.class, "main.war");
       main.addClasses(CassandraTestCase.class);
       ear.addAsModule(main);
       // ear.addAsManifestResource(new StringAsset("Dependencies: com.datastax.cassandra.driver-core \n"), "MANIFEST.MF");
       return ear;
   }

   protected static <T> T lookup(String beanName, Class<T> interfaceType) throws NamingException {
       return interfaceType.cast(iniCtx.lookup("java:global/" + ARCHIVE_NAME + "/" + "beans/" + beanName + "!" + interfaceType.getName()));
   }

    @Test
    public void jndiLookup() throws Exception {
        Object value = iniCtx.lookup("java:jboss/cassandradriver/test");
        if (value == null) {
            dumpJndi("java:jboss/cassandradriver");
        }

        assertTrue(value instanceof Cluster);
    }

    @Test
    public void testSimpleCreateAndLoadEntities() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        statefulTestBean.query();
    }

    @Test
    public void testAsyncQuery() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        statefulTestBean.asyncQuery();
    }

    private static void dumpJndi(String s) {
        try {
            dumpTreeEntry(iniCtx.list(s), s);
        } catch (NamingException ignore) {
        }
    }

    private static void dumpTreeEntry(NamingEnumeration<NameClassPair> list, String s) throws NamingException {
        System.out.println("\ndump " + s);
        while (list.hasMore()) {
            NameClassPair ncp = list.next();
            System.out.println(ncp.toString());
            if (s.length() == 0) {
                dumpJndi(ncp.getName());
            } else {
                dumpJndi(s + "/" + ncp.getName());
            }
        }
    }

}