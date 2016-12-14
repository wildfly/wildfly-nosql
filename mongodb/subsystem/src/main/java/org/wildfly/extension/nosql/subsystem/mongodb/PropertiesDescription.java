/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.nosql.subsystem.mongodb;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshaller;
import org.jboss.as.controller.AttributeParser;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.PropertiesAttributeDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;

import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class PropertiesDescription extends PersistentResourceDefinition {

    static final PropertiesAttributeDefinition PROPERTIES =
            new PropertiesAttributeDefinition.Builder(CommonAttributes.PROPERTY, true)
                    .setAllowExpression(true)
                    .setAttributeMarshaller(AttributeMarshaller.PROPERTIES_MARSHALLER_UNWRAPPED)
                    .setAttributeParser(AttributeParser.PROPERTIES_PARSER_UNWRAPPED)
                    .setMapValidator(new PropertiesValidator())
                    .build();

    private static final Collection<AttributeDefinition> ATTRIBUTES = Collections.singletonList(PROPERTIES);

    static final PropertiesDescription INSTANCE = new PropertiesDescription();

    private PropertiesDescription() {
        super(MongoDriverExtension.PROPERTIES_PATH,
                MongoDriverExtension.getResolver(CommonAttributes.PROFILE, CommonAttributes.PROPERTIES),
                new AbstractAddStepHandler(ATTRIBUTES), ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return ATTRIBUTES;
    }

}
