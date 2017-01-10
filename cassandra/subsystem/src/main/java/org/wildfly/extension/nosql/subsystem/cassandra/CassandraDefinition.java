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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.ValueManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.inject.CastingInjector;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.ImmediateValue;
import org.wildfly.extension.nosql.driver.cassandra.CassandraClientConnectionsService;
import org.wildfly.extension.nosql.driver.cassandra.ConfigurationBuilder;
import org.wildfly.nosql.common.ConnectionServiceAccess;

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

            final ModelNode profileEntry = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
            final Set<String> outboundSocketBindings = new HashSet<>();
            ConfigurationBuilder builder = new ConfigurationBuilder();
            if (profileEntry.hasDefined(CommonAttributes.ID_NAME)) {
                builder.setDescription(profileEntry.get(CommonAttributes.ID_NAME).asString());
            }
            if (profileEntry.hasDefined(CommonAttributes.JNDI_NAME)) {
                builder.setJNDIName(profileEntry.get(CommonAttributes.JNDI_NAME).asString());
            }
            if (profileEntry.hasDefined(CommonAttributes.MODULE_NAME)) {
                builder.setModuleName(profileEntry.get(CommonAttributes.MODULE_NAME).asString());
            }
            if (profileEntry.hasDefined(CommonAttributes.DATABASE)) {
                builder.setKeyspace(profileEntry.get(CommonAttributes.DATABASE).asString());
            }
            if (profileEntry.hasDefined(CommonAttributes.HOST_DEF)) {
                ModelNode hostModels = profileEntry.get(CommonAttributes.HOST_DEF);
                for (ModelNode host : hostModels.asList()) {
                    for (ModelNode hostEntry : host.get(0).asList()) {
                        if (hostEntry.hasDefined(CommonAttributes.OUTBOUND_SOCKET_BINDING_REF)) {
                            String outboundSocketBindingRef = hostEntry.get(CommonAttributes.OUTBOUND_SOCKET_BINDING_REF).asString();
                            outboundSocketBindings.add(outboundSocketBindingRef);
                        }
                    }
                }
            }
            startCassandraDriverService(context, builder, outboundSocketBindings);
        }

        private void startCassandraDriverService(OperationContext context, ConfigurationBuilder builder, final Set<String> outboundSocketBindings) throws OperationFailedException {
            if (builder.getJNDIName() != null && builder.getJNDIName().length() > 0) {
                final CassandraClientConnectionsService cassandraClientConnectionsService = new CassandraClientConnectionsService(builder);
                final ServiceName serviceName = ConnectionServiceAccess.serviceName(builder.getDescription());
                final ContextNames.BindInfo bindingInfo = ContextNames.bindInfoFor(builder.getJNDIName());

                final BinderService binderService = new BinderService(bindingInfo.getBindName());
                context.getServiceTarget().addService(bindingInfo.getBinderServiceName(), binderService)
                        .addDependency(CassandraSubsystemService.serviceName())
                        .addDependency(bindingInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                        .addDependency(serviceName, CassandraClientConnectionsService.class, new Injector<CassandraClientConnectionsService>() {
                            @Override
                            public void inject(final CassandraClientConnectionsService value) throws
                                    InjectionException {
                                binderService.getManagedObjectInjector().inject(new ValueManagedReferenceFactory(new ImmediateValue<>(value.getSession() != null ? value.getSession() : value.getCluster())));
                            }

                            @Override
                            public void uninject() {
                                binderService.getNamingStoreInjector().uninject();
                            }
                        }).install();

                final ServiceBuilder<CassandraClientConnectionsService> serviceBuilder = context.getServiceTarget().addService(serviceName, cassandraClientConnectionsService);
                serviceBuilder.addDependency(CassandraSubsystemService.serviceName(), new CastingInjector<>(cassandraClientConnectionsService.getCassandraSubsystemServiceInjectedValue(), CassandraSubsystemService.class));
                // add service dependency on each separate hostname/port reference in standalone*.xml referenced from this driver profile definition.
                for (final String outboundSocketBinding : outboundSocketBindings) {
                    final ServiceName outboundSocketBindingDependency = context.getCapabilityServiceName(CassandraDriverDefinition.OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME, outboundSocketBinding, OutboundSocketBinding.class);
                    serviceBuilder.addDependency(ServiceBuilder.DependencyType.REQUIRED, outboundSocketBindingDependency, OutboundSocketBinding.class, cassandraClientConnectionsService.getOutboundSocketBindingInjector(outboundSocketBinding));
                }
                serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();
            }
        }

    }

}
