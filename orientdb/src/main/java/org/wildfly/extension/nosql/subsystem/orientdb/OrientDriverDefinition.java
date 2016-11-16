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

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
final class OrientDriverDefinition extends PersistentResourceDefinition {

    private static final String DRIVER_SERVICE_CAPABILITY_NAME = "org.wildfly.nosql.orient.driver-service";

    static final String OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME = "org.wildfly.network.outbound-socket-binding";

    static final RuntimeCapability<Void> DRIVER_SERVICE_CAPABILITY = RuntimeCapability.Builder
            .of(DRIVER_SERVICE_CAPABILITY_NAME).build();

    private static final List<PersistentResourceDefinition> CHILDREN = Arrays.asList(OrientDefinition.INSTANCE);

    static final OrientDriverDefinition INSTANCE = new OrientDriverDefinition();

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Collections.emptyList();
    }

    private OrientDriverDefinition() {
        super(PathElement.pathElement(ModelDescriptionConstants.SUBSYSTEM, OrientDriverExtension.SUBSYSTEM_NAME),
                OrientDriverExtension.getResourceDescriptionResolver(), OrientDriverSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    @Override
    protected List<PersistentResourceDefinition> getChildren() {
        return CHILDREN;
    }

}
