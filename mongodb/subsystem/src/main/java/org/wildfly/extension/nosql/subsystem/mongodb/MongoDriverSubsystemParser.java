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

package org.wildfly.extension.nosql.subsystem.mongodb;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.AttributeParser;
import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLParser;

/**
 * MongoDriverSubsystemParser
 *
 * @author Scott Marlow
 */
final class MongoDriverSubsystemParser extends PersistentResourceXMLParser {
    protected static final MongoDriverSubsystemParser INSTANCE = new MongoDriverSubsystemParser();
    private static final PersistentResourceXMLDescription xmlDescription;

    static {
        xmlDescription = builder(MongoDriverDefinition.INSTANCE, MongoDriverExtension.CURRENT.getUriString()).
                addChild(
                        builder(MongoDefinition.INSTANCE)
                                .addAttributes(
                                        MongoDefinition.ID_NAME,
                                        MongoDefinition.JNDI_NAME,
                                        MongoDefinition.DATABASE,
                                        MongoDefinition.ADMIN_DATABASE,
                                        MongoDefinition.MODULE,
                                        MongoDefinition.SECURITY_DOMAIN,
                                        MongoDefinition.AUTH_TYPE,
                                        MongoDefinition.SSL,
                                        MongoDefinition.REPLICA_SET
                                )
                                .addChild(builder(HostDefinition.INSTANCE)
                                        .addAttributes(
                                                HostDefinition.OUTBOUND_SOCKET_BINDING_REF
                                        )
                                )
                                .addChild(builder(PropertiesDescription.INSTANCE)
                                        .addAttribute(PropertiesDescription.PROPERTIES,
                                                AttributeParser.PROPERTIES_PARSER_UNWRAPPED,
                                                AttributeMarshaller.PROPERTIES_MARSHALLER_UNWRAPPED)
                                )
                )
                .build();
    }

    @Override
    public PersistentResourceXMLDescription getParserDescription() {
        return xmlDescription;
    }

}
