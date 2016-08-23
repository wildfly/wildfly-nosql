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

package org.jboss.as.test.compat.nosql.neo4j.modules;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.inject.Named;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

/**
 * NestedBean
 *
 * @author Scott Marlow
 */
@Remote
@Stateful
public class NestedBean {

    @Inject
    @Named("neo4jtesttprofile")
    private Driver injectedDriver;

    @TransactionAttribute(REQUIRES_NEW)
    public String getPerson(String name) {
        Session session = injectedDriver.session();
        try {
            StatementResult result = session.run("MATCH (a:Person) WHERE a.name = '" + name + "' RETURN a.name AS name, a.title AS title");
            if (result.hasNext()) {
                Record record = result.next();
                return record.toString();
            }
            return name + " not found";
        }
        finally {
            session.close();
        }
    }

}
