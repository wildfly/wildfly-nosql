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
