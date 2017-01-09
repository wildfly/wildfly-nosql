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

package org.wildfly.extension.nosql.driver.neo4j.transaction;

import java.util.ArrayList;

/**
 * TransactionEnlistmentType
 *
 * @author Scott Marlow
 */
public enum TransactionEnlistmentType {
    NONE("none"),
    ONEPHASECOMMIT("1pc");
    // TWOPHASECOMMIT("2pc"); // future if/when Neo4j supports XAResource that we can use.

    private final String value;
    private static final ArrayList<String> allowedNames = new ArrayList<>();
    static {
        allowedNames.add(NONE.getValue());
        allowedNames.add(ONEPHASECOMMIT.getValue());
        // allowedNames.add(TWOPHASECOMMIT.getValue());
    }

    TransactionEnlistmentType(String value) {
            this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionEnlistmentType getFromStringValue(String find) {
        for(TransactionEnlistmentType value: values()) {
            if ( value != null && find.equals(value.getValue())) {
                return value;
            }
        }
        return null;
    }

    public static ArrayList allowedNames() {
        return allowedNames;
    }
}
