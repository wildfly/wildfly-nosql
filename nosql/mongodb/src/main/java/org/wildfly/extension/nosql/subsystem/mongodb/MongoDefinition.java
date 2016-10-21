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

package org.wildfly.extension.nosql.subsystem.mongodb;

import static org.wildfly.extension.nosql.subsystem.mongodb.MongoDriverDefinition.OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME;

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
import org.jboss.as.security.service.SubjectFactoryService;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.msc.inject.CastingInjector;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.security.SubjectFactory;
import org.wildfly.extension.nosql.driver.mongodb.ConfigurationBuilder;
import org.wildfly.extension.nosql.driver.mongodb.MongoClientConnectionsService;
import org.wildfly.extension.nosql.driver.mongodb.ReadConcernType;
import org.wildfly.extension.nosql.driver.mongodb.WriteConcernType;
import org.wildfly.nosql.common.ConnectionServiceAccess;

/**
 * MongoDefinition represents a target MongoDB database.
 *
 * @author Scott Marlow
 */
public class MongoDefinition extends PersistentResourceDefinition {

    private static final List<? extends PersistentResourceDefinition> CHILDREN;

    static {
        List<PersistentResourceDefinition> children = new ArrayList<>();
        children.add(HostDefinition.INSTANCE);
        children.add(PropertiesDescription.INSTANCE);
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

    protected static final SimpleAttributeDefinition SECURITY_DOMAIN =
            new SimpleAttributeDefinitionBuilder(CommonAttributes.SECURITY_DOMAIN, ModelType.STRING, true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setAllowExpression(false)
                    .build();


    protected static List<SimpleAttributeDefinition> ATTRIBUTES = Arrays.asList(
            ID_NAME,
            JNDI_NAME,
            DATABASE,
            MODULE,
            SECURITY_DOMAIN);

    static final Map<String, AttributeDefinition> ATTRIBUTES_MAP = new HashMap<>();

    static {
        for (SimpleAttributeDefinition attr : ATTRIBUTES) {
            ATTRIBUTES_MAP.put(attr.getName(), attr);
        }

    }

    static final MongoDefinition INSTANCE = new MongoDefinition();

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return ATTRIBUTES_MAP.values();
    }

    @Override
    public List<? extends PersistentResourceDefinition> getChildren() {
        return CHILDREN;
    }

    private MongoDefinition() {
        super(MongoDriverExtension.PROFILE_PATH,
                MongoDriverExtension.getResolver(CommonAttributes.PROFILE),
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
                builder.setDatabase(profileEntry.get(CommonAttributes.DATABASE).asString());
            }
            if (profileEntry.hasDefined(CommonAttributes.SECURITY_DOMAIN)) {
                builder.setSecurityDomain(profileEntry.get(CommonAttributes.SECURITY_DOMAIN).asString());
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
            if (profileEntry.hasDefined(CommonAttributes.PROPERTIES)) {
                for (ModelNode propertyProfiles : profileEntry.get(CommonAttributes.PROPERTIES).asList()) {
                    for (ModelNode propertyModels : propertyProfiles.get(0).asList()) {
                        if (propertyModels.hasDefined(CommonAttributes.PROPERTY)) {
                            for (Property property : propertyModels.get(CommonAttributes.PROPERTY).asPropertyList()) {
                                if (property.getName().equals(CommonAttributes.WRITE_CONCERN)) {
                                    builder.setWriteConcern(WriteConcernType.valueOf(property.getValue().asString()).name());
                                } else if (property.getName().equals(CommonAttributes.READ_CONCERN)) {
                                    builder.setReadConcern(ReadConcernType.valueOf(property.getValue().asString()).name());
                                }
                            }
                        }
                    }
                }
            }
            startMongoDriverService(context, builder, outboundSocketBindings);
        }

        private void startMongoDriverService(OperationContext context, ConfigurationBuilder builder, Set<String> outboundSocketBindings) {
            if (builder.getJNDIName() != null && builder.getJNDIName().length() > 0) {
                final MongoClientConnectionsService mongoClientConnectionsService = new MongoClientConnectionsService(builder);
                final ServiceName serviceName = ConnectionServiceAccess.serviceName(builder.getDescription());
                final ContextNames.BindInfo bindingInfo = ContextNames.bindInfoFor(builder.getJNDIName());

                final BinderService binderService = new BinderService(bindingInfo.getBindName());

                context.getServiceTarget().addService(bindingInfo.getBinderServiceName(), binderService)
                        .addDependency(MongoSubsystemService.serviceName())
                        .addDependency(bindingInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                        .addDependency(serviceName, MongoClientConnectionsService.class, new Injector<MongoClientConnectionsService>() {
                            @Override
                            public void inject(final MongoClientConnectionsService value) throws
                                    InjectionException {
                                binderService.getManagedObjectInjector().inject(new ValueManagedReferenceFactory(new ImmediateValue<>(value.getDatabase() != null ? value.getDatabase() : value.getClient())));
                            }

                            @Override
                            public void uninject() {
                                binderService.getNamingStoreInjector().uninject();
                            }
                        }).install();
                final ServiceBuilder<MongoClientConnectionsService> serviceBuilder = context.getServiceTarget().addService(serviceName, mongoClientConnectionsService);
                serviceBuilder.addDependency(MongoSubsystemService.serviceName(), new CastingInjector<>(mongoClientConnectionsService.getMongoSubsystemServiceInjectedValue(), MongoSubsystemService.class));
                // add service dependency on each separate hostname/port reference in standalone*.xml referenced from this driver profile definition.
                for (final String outboundSocketBinding : outboundSocketBindings) {
                    final ServiceName outboundSocketBindingDependency = context.getCapabilityServiceName(OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME, outboundSocketBinding, OutboundSocketBinding.class);
                    serviceBuilder.addDependency(ServiceBuilder.DependencyType.REQUIRED, outboundSocketBindingDependency, OutboundSocketBinding.class, mongoClientConnectionsService.getOutboundSocketBindingInjector(outboundSocketBinding));
                }
                if (builder.getSecurityDomain() != null) {
                    serviceBuilder.addDependency(SubjectFactoryService.SERVICE_NAME, SubjectFactory.class,
                            mongoClientConnectionsService.getSubjectFactoryInjector());
                }
                serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();
            }
        }
    }
}


