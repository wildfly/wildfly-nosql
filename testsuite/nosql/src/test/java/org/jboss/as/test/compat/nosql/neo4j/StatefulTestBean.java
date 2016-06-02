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

import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.inject.Inject;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.wildfly.nosql.ClientProfile;

/**
 * StatefulTestBean for the MongoDB document database
 *
 * @author Scott Marlow
 */
@ClientProfile(profile = "neo4jtesttprofile")
@Stateful
public class StatefulTestBean {

    // @Inject
    // private Session session;
    @Resource(lookup = "java:jboss/neo4jdriver/test")
    private Driver driver;

    public void addPerson() {
        Session session = driver.session();
        try {
            session.run("CREATE (a:Person {name:'Arthur', title:'King'})");
            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = 'Arthur' RETURN a.name AS name, a.title AS title");

            while (result.hasNext()) {
                Record record = result.next();
                System.out.println(record.get("title").asString() + " " + record.get("name").asString());
            }
        } finally {
            session.close();
        }
    }
}
