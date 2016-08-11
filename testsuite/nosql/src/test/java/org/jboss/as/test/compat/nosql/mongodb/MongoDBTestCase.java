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

package org.jboss.as.test.compat.nosql.mongodb;

import static org.junit.Assert.assertTrue;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.mongodb.client.MongoDatabase;
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
 * MongoDBTestCase
 *
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
public class MongoDBTestCase {
    private static final String ARCHIVE_NAME = "MongoDBTestCase_test";

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
        lib.addClasses(StatefulTestBean.class);
        ear.addAsModule(lib);
        final WebArchive main = ShrinkWrap.create(WebArchive.class, "main.war");
        main.addClasses(MongoDBTestCase.class);
        ear.addAsModule(main);
        // ear.addAsManifestResource(new StringAsset("Dependencies: org.mongodb.driver \n"), "MANIFEST.MF");
        return ear;
    }

    protected static <T> T lookup(String beanName, Class<T> interfaceType) throws NamingException {
        return interfaceType.cast(iniCtx.lookup("java:global/" + ARCHIVE_NAME + "/" + "beans/" + beanName + "!" + interfaceType.getName()));
    }

    @Test
    public void jndiLookup() throws Exception {
        Object value = iniCtx.lookup("java:jboss/mongodb/test");
        if (value == null) {
            dumpJndi("java:jboss/mongodb");
        }

        assertTrue(value instanceof MongoDatabase);
    }

    @Test
    public void testSimpleCreateAndLoadEntities() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        String comment = statefulTestBean.addUserComment();
        assertTrue(comment + " contains \"MongoDB Is Web Scale\"", comment.contains("MongoDB Is Web Scale"));
        String product = statefulTestBean.addProduct();
        assertTrue(product + " contains \"Acme products\"", product.contains("Acme products"));
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