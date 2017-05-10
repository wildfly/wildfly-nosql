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
 * Cassandra client driver subsystem ResourceDefinition
 */
public class CassandraDriverDefinition extends PersistentResourceDefinition {

    static final String DRIVER_SERVICE_CAPABILITY_NAME = "org.wildfly.nosql.cassandra.driver-service";
    static final String OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME = "org.wildfly.network.outbound-socket-binding";
    static final RuntimeCapability<Void> DRIVER_SERVICE_CAPABILITY =
            RuntimeCapability.Builder.of(DRIVER_SERVICE_CAPABILITY_NAME)
                    .build();

    public static final CassandraDriverDefinition INSTANCE = new CassandraDriverDefinition();

    static final PersistentResourceDefinition[] CHILDREN = {
            CassandraDefinition.INSTANCE
    };

    private CassandraDriverDefinition() {
        super(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, CassandraDriverExtension.SUBSYSTEM_NAME),
                CassandraDriverExtension.getResourceDescriptionResolver(),
                CassandraDriverSubsystemAdd.INSTANCE,
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
