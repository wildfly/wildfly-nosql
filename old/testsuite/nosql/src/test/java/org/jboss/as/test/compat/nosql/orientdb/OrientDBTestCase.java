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

package org.jboss.as.test.compat.nosql.orientdb;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
public class OrientDBTestCase extends AbstractTestCase {

    private static final String ARCHIVE_NAME = "OrientDBTestCase_test";

    @Inject
    private StatefulTestBean statefulTestBean;

    @Deployment
    public static Archive<?> deploy() throws Exception {
        JavaArchive beans = ShrinkWrap.create(JavaArchive.class, "beans.jar");
        beans.addClasses(StatefulTestBean.class, TestPeopleDao.class);

        WebArchive main = ShrinkWrap.create(WebArchive.class, "main.war");
        main.addClasses(OrientDBTestCase.class, AbstractTestCase.class);

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, ARCHIVE_NAME + ".ear");
        ear.addAsModules(beans, main);

        return ear;
    }

    @Test
    public void shouldAddAPersonToTheDatabase() {
        String name = "test-name-" + LocalTime.now();
        ODocument person = statefulTestBean.addPerson(name);
        assertEquals(name, person.field("name"));

        List<ODocument> people = statefulTestBean.getPeople();
        assertEquals(1, people.size());
        assertEquals(person, people.get(0));
    }

    @Test
    public void shouldAddAFriendshipToTheGraph() {
        String firstName = "test-name-" + LocalTime.now();
        String secondName = "test-name-" + LocalTime.now();
        OrientEdge edge = statefulTestBean.addFriend(firstName, secondName);
        assertEquals(firstName, edge.getVertex(Direction.OUT).getProperty("name"));
        assertEquals(secondName, edge.getVertex(Direction.IN).getProperty("name"));
        assertEquals("knows", edge.getLabel());

        List<Edge> edges = statefulTestBean.getFriends();
        assertEquals(1, edges.size());
        assertEquals(edge, edges.get(0));
    }

}
