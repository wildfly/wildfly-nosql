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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.naming.ServiceBasedNamingStore;
import org.jboss.as.naming.ValueManagedReferenceFactory;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.service.BinderService;
import org.jboss.as.network.OutboundSocketBinding;
import org.wildfly.extension.nosql.driver.mongodb.ConfigurationBuilder;
import org.wildfly.extension.nosql.driver.mongodb.MongoClientConnectionsService;
import org.wildfly.nosql.common.ConnectionServiceAccess;
import org.wildfly.nosql.common.DriverDependencyProcessor;
import org.wildfly.nosql.common.DriverScanDependencyProcessor;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.inject.InjectionException;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.ImmediateValue;

/**
 * MongoDriverSubsystemAdd
 *
 * @author Scott Marlow
 */
public class MongoDriverSubsystemAdd extends AbstractBoottimeAddStepHandler {

    public static final MongoDriverSubsystemAdd INSTANCE = new MongoDriverSubsystemAdd();

    private final ParametersValidator runtimeValidator = new ParametersValidator();

    private MongoDriverSubsystemAdd() {
        super(MongoDriverDefinition.DRIVER_SERVICE_CAPABILITY);
    }

    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (AttributeDefinition def : MongoDriverDefinition.INSTANCE.getAttributes()) {
            def.validateAndSet(operation, model);
        }
    }

    protected void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model) throws
            OperationFailedException {

        runtimeValidator.validate(operation.resolve());
        context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget processorTarget) {
                // TODO: create Phase.PARSE_MONGO_DRIVER to use instead of phase.PARSE_PERSISTENCE_UNIT + 10 hack
                processorTarget.addDeploymentProcessor(MongoDriverExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_PERSISTENCE_UNIT + 10, new DriverScanDependencyProcessor("mongodbsubsystem"));
                // TODO: create Phase.DEPENDENCIES_MONGO_DRIVER to use instead of phase.DEPENDENCIES_PERSISTENCE_ANNOTATION+10 hack
                processorTarget.addDeploymentProcessor(MongoDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_PERSISTENCE_ANNOTATION + 10, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        final ModelNode mongoSubsystem = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
        if (mongoSubsystem.hasDefined(CommonAttributes.PROFILE)) {
            Map<String, String> jndiNameToModuleName = new HashMap<>();
            Map<String, String> profileNameToModuleName = new HashMap<>();
            for (ModelNode profiles : mongoSubsystem.get(CommonAttributes.PROFILE).asList()) {
                final Set<String> outboundSocketBindings = new HashSet<>();
                ConfigurationBuilder builder = new ConfigurationBuilder();
                for (ModelNode profileEntry : profiles.get(0).asList()) {
                    if (profileEntry.hasDefined(CommonAttributes.ID_NAME)) {
                        builder.setDescription(profileEntry.get(CommonAttributes.ID_NAME).asString());
                    } else if (profileEntry.hasDefined(CommonAttributes.JNDI_NAME)) {
                        builder.setJNDIName(profileEntry.get(CommonAttributes.JNDI_NAME).asString());
                    } else if (profileEntry.hasDefined(CommonAttributes.MODULE_NAME)) {
                        builder.setModuleName(profileEntry.get(CommonAttributes.MODULE_NAME).asString());
                    } else if (profileEntry.hasDefined(CommonAttributes.DATABASE)) {
                        builder.setDatabase(profileEntry.get(CommonAttributes.DATABASE).asString());
                    } else if (profileEntry.hasDefined(CommonAttributes.HOST_DEF)) {
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
                }
                startMongoDriverService(context, builder, jndiNameToModuleName, profileNameToModuleName, outboundSocketBindings);
            }
            startMongoDriverSubsysteService(context, jndiNameToModuleName, profileNameToModuleName);

        }
    }

    private void startMongoDriverSubsysteService(OperationContext context, Map<String, String> jndiNameToModuleName, Map<String, String> profileNameToModuleName) {
        MongoSubsystemService mongoSubsystemService = new MongoSubsystemService(jndiNameToModuleName, profileNameToModuleName);
        context.getServiceTarget().addService(MongoSubsystemService.serviceName(), mongoSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }

    private void startMongoDriverService(OperationContext context, ConfigurationBuilder builder, Map jndiNameToModuleName, Map<String, String> profileNameToModuleName, Set<String> outboundSocketBindings) {
        if (builder.getJNDIName() != null && builder.getJNDIName().length() > 0) {
            final MongoClientConnectionsService mongoClientConnectionsService = new MongoClientConnectionsService(builder);
            final ServiceName serviceName = ConnectionServiceAccess.serviceName(builder.getDescription());
            final ContextNames.BindInfo bindingInfo = ContextNames.bindInfoFor(builder.getJNDIName());

            if (builder.getModuleName() != null) {
                // maintain a mapping from JNDI name to NoSQL module name, that we will use during deployment time to
                // identify the static module name to add to the deployment.
                jndiNameToModuleName.put(builder.getJNDIName(), builder.getModuleName());
                profileNameToModuleName.put(builder.getDescription(), builder.getModuleName());
            }

            final BinderService binderService = new BinderService(bindingInfo.getBindName());
            context.getServiceTarget().addService(bindingInfo.getBinderServiceName(), binderService)
                    .addDependency(bindingInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                    .addDependency(MongoSubsystemService.serviceName())
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
            // add service dependency on each separate hostname/port reference in standalone*.xml referenced from this driver profile definition.
            for (final String outboundSocketBinding : outboundSocketBindings) {
                final ServiceName outboundSocketBindingDependency = context.getCapabilityServiceName(OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME, outboundSocketBinding, OutboundSocketBinding.class);
                serviceBuilder.addDependency(ServiceBuilder.DependencyType.REQUIRED, outboundSocketBindingDependency, OutboundSocketBinding.class, mongoClientConnectionsService.getOutboundSocketBindingInjector(outboundSocketBinding));
            }
            serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();
        }
    }

}
