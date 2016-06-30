/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.compat.nosql.neo4j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import javax.naming.InitialContext;
import javax.naming.NamingException;

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
 * Neo4jDBTestCase
 *
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
public class Neo4jDBTestCase {
    private static final String ARCHIVE_NAME = "Neo4jDBTestCase_test";

    @ArquillianResource
    private static InitialContext iniCtx;

    @BeforeClass
    public static void beforeClass() throws NamingException {
        iniCtx = new InitialContext();
    }

    @Deployment
    public static Archive<?> deploy() throws Exception {

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, ARCHIVE_NAME + ".ear");
        // addTestJarsToEar(ear);
        JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "beans.jar");
        lib.addClasses(StatefulTestBean.class, NestedBean.class);
        ear.addAsModule(lib);
        final WebArchive main = ShrinkWrap.create(WebArchive.class, "main.war");
        main.addClasses(Neo4jDBTestCase.class);
        ear.addAsModule(main);
        return ear;
    }

    protected static <T> T lookup(String beanName, Class<T> interfaceType) throws NamingException {
        return interfaceType.cast(iniCtx.lookup("java:global/" + ARCHIVE_NAME + "/" + "beans/" + beanName + "!" + interfaceType.getName()));
    }

    @Test
    public void testSimpleCreateAndLoadEntities() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        String result = statefulTestBean.addPerson();
        assertEquals("Record<{name: \"Arthur\", title: \"King\"}>", result);
    }

    @Test
    public void testInjectedClassInstance() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        String result = statefulTestBean.addPersonClassInstanceInjection();
        assertEquals("Record<{name: \"CDI\", title: \"King\"}>", result);
    }

    /**
     * Verify that calling a session bean method that starts a JTA transaction, adds a database value and then calls nested bean method that
     * requires a new transaction, the nested bean method should not be able to read the database value as the controlling JTA transaction did
     * not get committed yet.
     */
    @Test
    public void testTransactionEnlistmentReadAfterTransactionClose() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        String result = statefulTestBean.transactionEnlistmentReadAfterCallingTransactionClose();
        if (result.equals("Record<{name: \"TRANSACTION\", title: \"King\"}>")) {
            fail("Should not be able to read 'TRANSACTION' value from database as the JTA transaction did not end yet.");
        }
        else if (result.equals("TRANSACTION not found")) {
            // success!
            // we expect that the database add of "TRANSACTION" will not occur yet, since the JTA transaction has
            // not ended when we attempt to read the "TRANSACTION" value.  "TRANSACTION not found" is the expected response.
        }
        else {
            fail("unexpected result = " + result);
        }
    }

}