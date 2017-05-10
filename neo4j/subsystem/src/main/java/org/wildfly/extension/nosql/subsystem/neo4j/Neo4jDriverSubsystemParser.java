/*
 * *
 *  * Copyright 2017 Red Hat, Inc, and individual contributors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wildfly.extension.nosql.subsystem.neo4j;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLParser;

/**
 * Neo4jDriverSubsystemParser
 *
 * @author Scott Marlow
 */
final class Neo4jDriverSubsystemParser extends PersistentResourceXMLParser {
    protected static final Neo4jDriverSubsystemParser INSTANCE = new Neo4jDriverSubsystemParser();
    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription = builder(Neo4jDriverDefinition.INSTANCE, Neo4jDriverExtension.CURRENT.getUriString()).
                addChild(
                        builder(Neo4jDefinition.INSTANCE)
                                .addAttributes(
                                        Neo4jDefinition.ID_NAME,
                                        Neo4jDefinition.JNDI_NAME,
                                        Neo4jDefinition.MODULE,
                                        Neo4jDefinition.TRANSACTION
                                )
                                .addChild(builder(HostDefinition.INSTANCE)
                                        .addAttributes(
                                                HostDefinition.OUTBOUND_SOCKET_BINDING_REF
                                        )
                                )
                )
                .build();
    }

    @Override
    public PersistentResourceXMLDescription getParserDescription() {
        return xmlDescription;
    }

}
