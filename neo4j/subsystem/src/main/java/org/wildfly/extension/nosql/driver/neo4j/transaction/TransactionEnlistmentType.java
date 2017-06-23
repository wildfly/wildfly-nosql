/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
