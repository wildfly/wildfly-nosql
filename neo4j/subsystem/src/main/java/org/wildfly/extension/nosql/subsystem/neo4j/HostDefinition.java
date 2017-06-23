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

package org.wildfly.extension.nosql.subsystem.neo4j;

import static org.wildfly.extension.nosql.subsystem.neo4j.Neo4jDriverDefinition.DRIVER_SERVICE_CAPABILITY;
import static org.wildfly.extension.nosql.subsystem.neo4j.Neo4jDriverDefinition.OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * HostDefinition
 *
 * @author Scott Marlow
 */
public class HostDefinition extends PersistentResourceDefinition {

    public static final SimpleAttributeDefinition OUTBOUND_SOCKET_BINDING_REF = new SimpleAttributeDefinitionBuilder(CommonAttributes.OUTBOUND_SOCKET_BINDING_REF, ModelType.STRING, false)
            .setAllowExpression(true)
            .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
            .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SOCKET_BINDING_REF)
            .setCapabilityReference(OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME, DRIVER_SERVICE_CAPABILITY)
            .build();


    protected static List<SimpleAttributeDefinition> ATTRIBUTES = Arrays.asList(
            OUTBOUND_SOCKET_BINDING_REF);

    static final Map<String, AttributeDefinition> ATTRIBUTES_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition attr : ATTRIBUTES) {
            ATTRIBUTES_MAP.put(attr.getName(), attr);
        }

    }

    static final HostDefinition INSTANCE = new HostDefinition();

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return ATTRIBUTES_MAP.values();
    }

    private HostDefinition() {
        super(Neo4jDriverExtension.HOST_PATH,
                Neo4jDriverExtension.getResolver(CommonAttributes.HOST_DEF),
                HostAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    private static class HostAdd extends AbstractAddStepHandler {
        private static final HostAdd INSTANCE = new HostAdd();

        private HostAdd() {
            super(ATTRIBUTES);
        }

        @Override
        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        }
    }

}
