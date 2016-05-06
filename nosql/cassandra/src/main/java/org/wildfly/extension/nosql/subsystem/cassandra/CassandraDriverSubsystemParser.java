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
                                        CassandraDefinition.MODULE
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
