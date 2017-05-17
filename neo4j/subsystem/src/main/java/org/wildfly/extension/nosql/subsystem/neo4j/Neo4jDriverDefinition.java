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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;

/**
 * Neo4J client driver subsystem ResourceDefinition
 */
public class Neo4jDriverDefinition extends PersistentResourceDefinition {

    static final String DRIVER_SERVICE_CAPABILITY_NAME = "org.wildfly.nosql.neo4j.driver-service";
    static final String OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME = "org.wildfly.network.outbound-socket-binding";
    static final RuntimeCapability<Void> DRIVER_SERVICE_CAPABILITY =
            RuntimeCapability.Builder.of(DRIVER_SERVICE_CAPABILITY_NAME)
                    .build();

    public static final Neo4jDriverDefinition INSTANCE = new Neo4jDriverDefinition();

    static final PersistentResourceDefinition[] CHILDREN = {
            Neo4jDefinition.INSTANCE
    };

    private Neo4jDriverDefinition() {
        super(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, Neo4jDriverExtension.SUBSYSTEM_NAME),
                Neo4jDriverExtension.getResourceDescriptionResolver(),
                Neo4jDriverSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    public Collection<AttributeDefinition> getAttributes() {
        return Collections.emptySet();
    }

    @Override
    protected List<? extends PersistentResourceDefinition> getChildren() {
        return Arrays.asList(CHILDREN);
    }

}
