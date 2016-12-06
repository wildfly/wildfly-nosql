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

package org.jboss.as.test.compat.nosql.orientdb.jaxrs;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.test.compat.nosql.orientdb.AbstractTestCase;
import org.jboss.as.test.compat.nosql.orientdb.TestPeopleDao;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.NamingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URL;
import java.time.LocalTime;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class BasicClientTestCase extends AbstractTestCase {

    private static final String ARCHIVE_NAME = "BasicClientTestCase_test";

    @Deployment
    public static WebArchive deploy() {
        return ShrinkWrap.create(WebArchive.class, ARCHIVE_NAME + ".war")
                .addClasses(AbstractTestCase.class, BasicClientTestCase.class, ClientResource.class, TestPeopleDao.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(WebXml.get("<servlet-mapping>\n<servlet-name>javax.ws.rs.core.Application</servlet-name>\n"
                        + "<url-pattern>/*</url-pattern>\n</servlet-mapping>\n"), "web.xml");
    }

    @ArquillianResource
    private URL url;

    private Client client;

    @Before
    public void before() throws NamingException {
        super.before();
        client = ClientBuilder.newClient();
    }

    @After
    public void after() throws NamingException {
        super.after();
        if (client != null) {
            client.close();
        }
    }

    @Test
    public void shouldCreateAndGetAPerson() {
        String name = "test-name-" + LocalTime.now();
        client.target(url.toExternalForm() + "client/" + ClientResource.PERSON_PATH).queryParam("name", name).request()
                .post(null);
        String foundName = client.target(url.toExternalForm() + "client/" + ClientResource.PERSON_PATH).queryParam("name", name)
                .request().get(String.class);

        assertEquals(name, foundName);
    }

}
