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

package org.jboss.as.test.compat.nosql.mongodb.modules;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.as.test.integration.management.util.ModelUtil.createOpNode;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.mongodb.client.MongoDatabase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.integration.management.base.ContainerResourceMgmtTestBase;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * CustomModuleTestCase
 *
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
@Ignore // stopped working when I added @Deployment
public class CustomModuleTestCase extends ContainerResourceMgmtTestBase {

    private static InitialContext iniCtx;

    private static final String ARCHIVE_NAME = "CustomModuleTestCase_test";

    @Deployment
    public static Archive<?> deploy() throws Exception {

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, ARCHIVE_NAME + ".ear");
        JavaArchive lib = ShrinkWrap.create(JavaArchive.class, "beans.jar");
        lib.addPackage(StatefulTestBean.class.getPackage());
        lib.addClasses(MgmtOperationException.class);
        ear.addAsModule(lib);
        final WebArchive main = ShrinkWrap.create(WebArchive.class, "main.war");
        main.addClasses(CustomModuleTestCase.class);
        ear.addAsModule(main);
        return ear;
    }

    protected static <T> T lookup(String beanName, Class<T> interfaceType) throws NamingException {
        return interfaceType.cast(iniCtx.lookup("java:global/" + ARCHIVE_NAME + "/" + "beans/" + beanName + "!" + interfaceType.getName()));
    }

    @Test
    public void jndiLookup() throws Exception {
        Object value = iniCtx.lookup("java:jboss/mongodb/test");
        assertTrue(value instanceof MongoDatabase);
    }

    @Test
    public void testSimpleCreateAndLoadEntities() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        statefulTestBean.addUserComment();
        statefulTestBean.addProduct();
    }

    @Test
    public void testHasTestModuleSlot() throws IOException, MgmtOperationException {
        ModelNode result = executeOperation(createOpNode("subsystem=mongodb/mongo=default", "read-resource"));

        assertTrue("contains database : " + result.toJSONString(true), result.get("database").isDefined());
        assertTrue("contains database=mongotestdb : " + result.toJSONString(true), result.get("database").asString().equals("mongotestdb"));

        assertTrue("contains module : " + result.toJSONString(true), result.get("module").isDefined());
        assertTrue("contains module=org.mongodb.driver:test : " + result.toJSONString(true), result.get("module").asString().equals("org.mongodb.driver:test"));
    }

    @BeforeClass
    public static void beforeClass() throws NamingException {
        final Hashtable env = new Hashtable();
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        env.put(Context.INITIAL_CONTEXT_FACTORY, org.jboss.naming.remote.client.InitialContextFactory.class.getName());
        env.put(Context.PROVIDER_URL, "remote://" + TestSuiteEnvironment.getServerAddress() + ":" + 4447);
        iniCtx = new InitialContext(env);
    }

    @Before
    public void doSetup() throws Exception {
        ModelNode address = new ModelNode();
        address.add("subsystem", "mongodb");
        address.add("mongo", "default");
        address.protect();

        final ModelNode operation = new ModelNode();
        operation.get(OP_ADDR).set(address);
        operation.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        operation.get(NAME).set("module");
        operation.get(VALUE).set("org.mongodb.driver:test");
        ModelNode result = executeOperation(operation);
    }

    @After
    public void tearDown() throws Exception {
        ModelNode address = new ModelNode();
        address.add("subsystem", "mongodb");
        address.add("mongo", "default");
        address.protect();

        final ModelNode operation = new ModelNode();
        operation.get(OP_ADDR).set(address);
        operation.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        operation.get(NAME).set("module");
        operation.get(VALUE).set("org.mongodb.driver");
        ModelNode result = executeOperation(operation);
    }
}


