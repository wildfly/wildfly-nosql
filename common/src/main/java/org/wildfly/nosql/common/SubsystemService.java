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

package org.wildfly.nosql.common;

import java.util.Collection;

/**
 * One SubsystemService implementation for each NoSQL driver subsystem.
 *
 * @author Scott Marlow
 */
public interface SubsystemService {

    /**
     * Convert JNDI name to module name for resolving the NoSQL module to inject into deployments.
     * Each NoSQL subsystem knows the NoSQL (driver) module that is mapped to the JNDI name.
     *
     * @param jndiName
     * @return
     */
    String moduleNameFromJndi(String jndiName);

    String moduleNameFromProfile(String profileName);

    /**
     * get profile names.
     * @return collection of NoSQL profile names
     */
    Collection<String> profileNames();

    /**
     * get jndi names.
     * @return collection of NoSQL defined jndi names.
     */
    Collection<String> jndiNames();

    /**
     * get vendor key that can be used as a key.
     *
     * @return unique key identifying the NoSQL database vendor (e.g. MongoDB/Cassandra/OrientDB/Neo4j)
     */
    String vendorKey();

}
