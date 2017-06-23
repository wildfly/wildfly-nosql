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

package org.wildfly.extension.nosql.subsystem.orientdb;

import java.util.Arrays;
import java.util.Collection;

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
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class HostDefinition extends PersistentResourceDefinition {

    private static final SimpleAttributeDefinition OUTBOUND_SOCKET_BINDING_REF =
            new SimpleAttributeDefinitionBuilder(CommonAttributes.OUTBOUND_SOCKET_BINDING_REF, ModelType.STRING, false)
                    .setAllowExpression(true)
                    .setValidator(new StringLengthValidator(1, Integer.MAX_VALUE, false, true))
                    .addAccessConstraint(SensitiveTargetAccessConstraintDefinition.SOCKET_BINDING_REF)
                    .setCapabilityReference(OrientDriverDefinition.OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME,
                            OrientDriverDefinition.DRIVER_SERVICE_CAPABILITY)
                    .build();

    static final AttributeDefinition[] ATTRIBUTES = { OUTBOUND_SOCKET_BINDING_REF };

    static final HostDefinition INSTANCE = new HostDefinition();

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(ATTRIBUTES);
    }

    private HostDefinition() {
        super(OrientDriverExtension.HOST_PATH,
                OrientDriverExtension.getResourceDescriptionResolver(CommonAttributes.ORIENT, CommonAttributes.HOST),
                HostAdd.INSTANCE, ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    private static class HostAdd extends AbstractAddStepHandler {

        private static final HostAdd INSTANCE = new HostAdd();

        private HostAdd() {
            super(ATTRIBUTES);
        }

        @Override
        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
                throws OperationFailedException {

        }

    }

}
