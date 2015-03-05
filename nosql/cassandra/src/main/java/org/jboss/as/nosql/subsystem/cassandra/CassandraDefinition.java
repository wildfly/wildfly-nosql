/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.as.nosql.subsystem.cassandra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * CassandraDefinition represents a target Cassandra database.
 *
 * @author Scott Marlow
 */
public class CassandraDefinition extends PersistentResourceDefinition {

    private static final List<? extends PersistentResourceDefinition> CHILDREN;

    static {
        List<PersistentResourceDefinition> children = new ArrayList<>();
        children.add(HostDefinition.INSTANCE);
        CHILDREN = Collections.unmodifiableList(children);
    }

    protected static final SimpleAttributeDefinition ID_NAME =
            new SimpleAttributeDefinitionBuilder(CommonAttributes.ID_NAME, ModelType.STRING, true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setAllowExpression(true)
                    .build();

    protected static final SimpleAttributeDefinition JNDI_NAME =
            new SimpleAttributeDefinitionBuilder(CommonAttributes.JNDI_NAME, ModelType.STRING, true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setAllowExpression(true)
                    .build();

    protected static final SimpleAttributeDefinition DATABASE =
            new SimpleAttributeDefinitionBuilder(CommonAttributes.DATABASE, ModelType.STRING, true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setAllowExpression(true)
                    .build();

    protected static final SimpleAttributeDefinition MODULE =
            new SimpleAttributeDefinitionBuilder(CommonAttributes.MODULE_NAME, ModelType.STRING, true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setAllowExpression(false)
                    .build();


    protected static List<SimpleAttributeDefinition> ATTRIBUTES = Arrays.asList(
            ID_NAME,
            JNDI_NAME,
            DATABASE,
            MODULE);

    static final Map<String, AttributeDefinition> ATTRIBUTES_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition attr : ATTRIBUTES) {
            ATTRIBUTES_MAP.put(attr.getName(), attr);
        }

    }

    static final CassandraDefinition INSTANCE = new CassandraDefinition();

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return ATTRIBUTES_MAP.values();
    }

    @Override
    public List<? extends PersistentResourceDefinition> getChildren() {
        return CHILDREN;
    }

    private CassandraDefinition() {
        super(CassandraDriverExtension.PROFILE_PATH,
                CassandraDriverExtension.getResolver(CommonAttributes.PROFILE),
                ProfileAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE);
    }

    private static class ProfileAdd extends AbstractAddStepHandler {

        private static final ProfileAdd INSTANCE = new ProfileAdd();

        private ProfileAdd() {
            super(ATTRIBUTES);
        }

        @Override
        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        }
    }

}
