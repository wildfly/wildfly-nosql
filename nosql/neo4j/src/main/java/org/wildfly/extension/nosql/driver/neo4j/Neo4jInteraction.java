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

package org.wildfly.extension.nosql.driver.neo4j;

import org.jboss.msc.service.StartException;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 * Neo4jInteraction is for interacting with Neo4j without static references to Neo4j classes.
 *
 * @author Scott Marlow
 */
public class Neo4jInteraction {
    StringBuffer builder = new StringBuffer("");

    protected Driver build() throws StartException {
        // TODO: switch to public static Driver driver( String url, AuthToken authToken, Config config )
        return GraphDatabase.driver(builder.toString());
    }

    protected void withPort(int port) throws StartException {
        add(":");
        add(Integer.toString(port));
    }

    protected void addContactPoint(String host) throws StartException {
        add(host);
    }

    private void add(String value) {
        if(builder.length() == 0) {
            builder.append("bolt://");
        }
        builder.append(value);
    }

    protected void driverClose(Driver driver) throws Throwable {
        driver.close();
    }

}