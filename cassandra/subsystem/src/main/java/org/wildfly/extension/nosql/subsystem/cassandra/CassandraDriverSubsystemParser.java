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

package org.wildfly.extension.nosql.subsystem.cassandra;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLParser;

/**
 * CassandraDriverSubsystemParser
 *
 * @author Scott Marlow
 */
final class CassandraDriverSubsystemParser extends PersistentResourceXMLParser {
    protected static final CassandraDriverSubsystemParser INSTANCE = new CassandraDriverSubsystemParser();
    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription = builder(CassandraDriverDefinition.INSTANCE, CassandraDriverExtension.CURRENT.getUriString()).
                addChild(
                        builder(CassandraDefinition.INSTANCE)
                                .addAttributes(
                                        CassandraDefinition.ID_NAME,
                                        CassandraDefinition.JNDI_NAME,
                                        CassandraDefinition.DATABASE,
                                        CassandraDefinition.MODULE,
                                        CassandraDefinition.SSL
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
