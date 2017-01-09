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

package org.wildfly.extension.nosql.subsystem.orientdb;

import static org.jboss.as.controller.PersistentResourceXMLDescription.builder;

import org.jboss.as.controller.PersistentResourceXMLDescription;
import org.jboss.as.controller.PersistentResourceXMLParser;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
final class OrientDriverSubsystemParser extends PersistentResourceXMLParser {

    static final OrientDriverSubsystemParser INSTANCE = new OrientDriverSubsystemParser();

    private static final PersistentResourceXMLDescription XML_DESCRIPTION =
            builder(OrientDriverDefinition.INSTANCE, OrientDriverExtension.CURRENT.getUriString())
                    .addChild(builder(OrientDefinition.INSTANCE)
                            .addAttributes(OrientDefinition.ATTRIBUTES)
                            .addChild(builder(HostDefinition.INSTANCE)
                                    .addAttributes(HostDefinition.ATTRIBUTES)
                            )
                    ).build();

    @Override
    public PersistentResourceXMLDescription getParserDescription() {
        return XML_DESCRIPTION;
    }

}
