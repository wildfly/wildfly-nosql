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

package org.jboss.as.test.compat.nosql.multiple;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
// import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import com.mongodb.client.MongoDatabase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.test.integration.management.base.AbstractMgmtTestBase;
import org.jboss.as.test.integration.management.base.ContainerResourceMgmtTestBase;
import org.jboss.as.test.integration.management.util.MgmtOperationException;
import org.jboss.as.test.shared.ServerReload;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * CustomModuleTestCase
 *
 * @author Scott Marlow
 */
@RunWith(Arquillian.class)
@ServerSetup(CustomModuleTestCase.TestCaseSetup.class)
public class CustomModuleTestCase  {

    @ArquillianResource
    private static InitialContext iniCtx;

    private static final String ARCHIVE_NAME = "CustomModuleTestCase_test";

    @Deployment
    public static JavaArchive deploy() throws Exception {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "beans.jar");
        jar.addPackage(StatefulTestBean.class.getPackage());
        jar.addClasses(MgmtOperationException.class);
        jar.addClasses(CustomModuleTestCase.class, MgmtOperationException.class, XMLElementReader.class,
                XMLElementWriter.class, AbstractMgmtTestBase.class, ContainerResourceMgmtTestBase.class)
                .addAsManifestResource(
                        new StringAsset(
                                "Dependencies: org.jboss.as.controller-client,org.jboss.dmr,org.jboss.as.cli \n"),
                        "MANIFEST.MF");
        return jar;
    }

    protected static <T> T lookup(String beanName, Class<T> interfaceType) throws NamingException {
        // String find = "java:global/" + ARCHIVE_NAME + "/" + "beans/" + beanName + "!" + interfaceType.getName();
        String find = "java:global/" + "beans/" + beanName + "!" + interfaceType.getName();
        try {
            return interfaceType.cast(iniCtx.lookup(find));
        } catch(NameNotFoundException e) {
            dumpJndi(find);
            throw e;
        }
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

    @Test
    public void jndiLookup() throws Exception {
        Object value = iniCtx.lookup("java:jboss/mongodb/test");
        if (value == null) {
              dumpJndi("java:jboss/mongodb/test");
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


    @Test
    public void testHasTestModuleSlot() throws Exception {
        StatefulTestBean statefulTestBean = lookup("StatefulTestBean", StatefulTestBean.class);
        String classLoaderName = statefulTestBean.getNoSQLClassLoader().toString();
        assertTrue(classLoaderName + " contains module org.mongodb.driver:test : " + classLoaderName, classLoaderName.contains("org.mongodb.driver:test"));
    }


    @BeforeClass
    public static void beforeClass() throws NamingException {
        final Hashtable env = new Hashtable();
        env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        env.put(Context.INITIAL_CONTEXT_FACTORY, org.jboss.naming.remote.client.InitialContextFactory.class.getName());
        env.put(Context.PROVIDER_URL, "remote://" + TestSuiteEnvironment.getServerAddress() + ":" + 4447);
        iniCtx = new InitialContext(env);
    }


    static class TestCaseSetup extends ContainerResourceMgmtTestBase implements ServerSetupTask {


        @Override
        public final void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            setManagementClient(managementClient);
            ModelNode address = new ModelNode();
            address.add("subsystem", "mongodb");
            address.add("mongo", "sales");
            address.protect();

            /*
            <mongo name="sales" id="mongodbsales" jndi-name="java:jboss/mongodb/sales" database="mongotestdb">
                <host name="default" outbound-socket-binding-ref="mongotesthost"/>
                <properties name="sales">
                    <property name="writeConcern" value="ACKNOWLEDGED"/>
                </properties>
            </mongo>
            */

            final ModelNode operation = new ModelNode();
            operation.get(OP_ADDR).set(address);
            operation.get(OP).set(ADD);
            operation.get(NAME).set("id");
            operation.get(VALUE).set("mongodbsales");
            operation.get(NAME).set("jndi-name");
            operation.get(VALUE).set("java:jboss/mongodb/sales");
            operation.get(NAME).set("database");
            operation.get(VALUE).set("mongotestdb");

            ModelNode result = executeOperation(operation);
            reload();
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
/*
            ModelNode address = new ModelNode();
            address.add("subsystem", "mongodb");
            address.add("mongo", "default");
            address.protect();

            final ModelNode operation = new ModelNode();
            operation.get(OP_ADDR).set(address);
            operation.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
            operation.get(NAME).set("module");
            operation.get(VALUE).set("org.mongodb.driver:main");
            ModelNode result = executeOperation(operation);
            reload();
*/
        }

        public void reload() throws Exception {
            ServerReload.executeReloadAndWaitForCompletion(getModelControllerClient(), 50000);
        }

    }
}


