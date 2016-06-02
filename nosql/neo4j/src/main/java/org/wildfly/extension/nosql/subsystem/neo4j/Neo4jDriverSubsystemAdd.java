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

package org.wildfly.extension.nosql.subsystem.neo4j;

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
import org.wildfly.extension.nosql.driver.neo4j.ConfigurationBuilder;
import org.wildfly.extension.nosql.driver.neo4j.Neo4jClientConnectionService;
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
 * Neo4jDriverSubsystemAdd
 *
 * @author Scott Marlow
 */
public class Neo4jDriverSubsystemAdd extends AbstractBoottimeAddStepHandler {

    public static final Neo4jDriverSubsystemAdd INSTANCE = new Neo4jDriverSubsystemAdd();
    private final ParametersValidator runtimeValidator = new ParametersValidator();

    private Neo4jDriverSubsystemAdd() {
        super(Neo4jDriverDefinition.DRIVER_SERVICE_CAPABILITY);
    }

    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (AttributeDefinition def : Neo4jDriverDefinition.INSTANCE.getAttributes()) {
            def.validateAndSet(operation, model);
        }
    }

    protected void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model) throws
            OperationFailedException {

        runtimeValidator.validate(operation.resolve());
        context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget processorTarget) {
                // TODO: create Phase.PARSE_CASSANDRA_DRIVER to use instead of phase.PARSE_PERSISTENCE_UNIT + 4 hack
                processorTarget.addDeploymentProcessor(Neo4jDriverExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_PERSISTENCE_UNIT + 4, new DriverScanDependencyProcessor("neo4jsubsystem"));
                // TODO: create Phase.DEPENDENCIES_CASSANDRA_DRIVER to use instead of phase.PARSE_PERSISTENCE_UNIT + 4 hack
                processorTarget.addDeploymentProcessor(Neo4jDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_PERSISTENCE_ANNOTATION + 4, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        final ModelNode neo4jSubsystem = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
        if (neo4jSubsystem.hasDefined(CommonAttributes.PROFILE)) {
            Map<String, String> jndiNameToModuleName = new HashMap<>();
            Map<String, String> profileNameToModuleName = new HashMap<>();
            for (ModelNode profiles : neo4jSubsystem.get(CommonAttributes.PROFILE).asList()) {
                final Set<String> outboundSocketBindings = new HashSet<>();
                ConfigurationBuilder builder = new ConfigurationBuilder();
                for (ModelNode profileEntry : profiles.get(0).asList()) {
                    if (profileEntry.hasDefined(CommonAttributes.ID_NAME)) {
                        builder.setDescription(profileEntry.get(CommonAttributes.ID_NAME).asString());
                    } else if (profileEntry.hasDefined(CommonAttributes.JNDI_NAME)) {
                        builder.setJNDIName(profileEntry.get(CommonAttributes.JNDI_NAME).asString());
                    } else if (profileEntry.hasDefined(CommonAttributes.MODULE_NAME)) {
                        builder.setModuleName(profileEntry.get(CommonAttributes.MODULE_NAME).asString());
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
                startNeo4jDriverService(context, builder, jndiNameToModuleName, profileNameToModuleName, outboundSocketBindings);
            }
            startNeo4jDriverSubsysteService(context, jndiNameToModuleName, profileNameToModuleName);
        }
    }

    private void startNeo4jDriverSubsysteService(final OperationContext context, final Map<String, String> jndiNameToModuleName, Map<String, String> profileNameToModuleName) {
        Neo4jSubsystemService neo4jSubsystemService = new Neo4jSubsystemService(jndiNameToModuleName, profileNameToModuleName);
        context.getServiceTarget().addService(Neo4jSubsystemService.serviceName(), neo4jSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }

    private void startNeo4jDriverService(OperationContext context, ConfigurationBuilder builder, Map<String, String> jndiNameToModuleName, Map<String, String> profileNameToModuleName, final Set<String> outboundSocketBindings) throws OperationFailedException {
        if (builder.getJNDIName() != null && builder.getJNDIName().length() > 0) {
            final Neo4jClientConnectionService neo4jClientConnectionService = new Neo4jClientConnectionService(builder);
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
                    .addDependency(Neo4jSubsystemService.serviceName())
                    .addDependency(bindingInfo.getParentContextServiceName(), ServiceBasedNamingStore.class, binderService.getNamingStoreInjector())
                    .addDependency(serviceName, Neo4jClientConnectionService.class, new Injector<Neo4jClientConnectionService>() {
                        @Override
                        public void inject(final Neo4jClientConnectionService value) throws
                                InjectionException {
                            binderService.getManagedObjectInjector().inject(new ValueManagedReferenceFactory(new ImmediateValue<>(value.getDriver())));
                        }

                        @Override
                        public void uninject() {
                            binderService.getNamingStoreInjector().uninject();
                        }
                    }).install();

            final ServiceBuilder<Neo4jClientConnectionService> serviceBuilder = context.getServiceTarget().addService(serviceName, neo4jClientConnectionService);
            // add service dependency on each separate hostname/port reference in standalone*.xml referenced from this driver profile definition.
            for (final String outboundSocketBinding : outboundSocketBindings) {
                final ServiceName outboundSocketBindingDependency = context.getCapabilityServiceName(Neo4jDriverDefinition.OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME, outboundSocketBinding, OutboundSocketBinding.class);
                serviceBuilder.addDependency(ServiceBuilder.DependencyType.REQUIRED, outboundSocketBindingDependency, OutboundSocketBinding.class, neo4jClientConnectionService.getOutboundSocketBindingInjector(outboundSocketBinding));
            }
            serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();
        }
    }

}
