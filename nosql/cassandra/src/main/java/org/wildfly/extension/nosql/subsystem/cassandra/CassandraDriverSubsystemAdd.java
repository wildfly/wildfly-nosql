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
import org.wildfly.extension.nosql.driver.cassandra.CassandraClientConnectionsService;
import org.wildfly.extension.nosql.driver.cassandra.ConfigurationBuilder;
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
 * CassandraDriverSubsystemAdd
 *
 * @author Scott Marlow
 */
public class CassandraDriverSubsystemAdd extends AbstractBoottimeAddStepHandler {

    public static final CassandraDriverSubsystemAdd INSTANCE = new CassandraDriverSubsystemAdd();
    private final ParametersValidator runtimeValidator = new ParametersValidator();

    private CassandraDriverSubsystemAdd() {
        super(CassandraDriverDefinition.DRIVER_SERVICE_CAPABILITY);
    }

    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        for (AttributeDefinition def : CassandraDriverDefinition.INSTANCE.getAttributes()) {
            def.validateAndSet(operation, model);
        }
    }

    protected void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model) throws
            OperationFailedException {

        runtimeValidator.validate(operation.resolve());
        context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget processorTarget) {
                // TODO: create Phase.PARSE_CASSANDRA_DRIVER to use instead of phase.PARSE_PERSISTENCE_UNIT + 1 hack
                processorTarget.addDeploymentProcessor(CassandraDriverExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_PERSISTENCE_UNIT + 1, new DriverScanDependencyProcessor("cassandrasubsystem"));
                // TODO: create Phase.DEPENDENCIES_CASSANDRA_DRIVER to use instead of phase.PARSE_PERSISTENCE_UNIT + 1 hack
                processorTarget.addDeploymentProcessor(CassandraDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_PERSISTENCE_ANNOTATION + 1, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        final ModelNode cassandraSubsystem = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
        if (cassandraSubsystem.hasDefined(CommonAttributes.PROFILE)) {
            Map<String, String> jndiNameToModuleName = new HashMap<>();
            for (ModelNode profiles : cassandraSubsystem.get(CommonAttributes.PROFILE).asList()) {
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
                        builder.setKeyspace(profileEntry.get(CommonAttributes.DATABASE).asString());
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
                startCassandraDriverService(context, builder, jndiNameToModuleName, outboundSocketBindings);
            }
            startCassandraDriverSubsysteService(context, jndiNameToModuleName);
        }
    }

    private void startCassandraDriverSubsysteService(final OperationContext context, final Map<String, String> jndiNameToModuleName) {
        CassandraSubsystemService cassandraSubsystemService = new CassandraSubsystemService(jndiNameToModuleName);
        context.getServiceTarget().addService(CassandraSubsystemService.serviceName(), cassandraSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }

    private void startCassandraDriverService(OperationContext context, ConfigurationBuilder builder, Map<String, String> jndiNameToModuleName, final Set<String> outboundSocketBindings) throws OperationFailedException {
        if (builder.getJNDIName() != null && builder.getJNDIName().length() > 0) {
            final CassandraClientConnectionsService cassandraClientConnectionsService = new CassandraClientConnectionsService(builder);
            final ServiceName serviceName = ConnectionServiceAccess.serviceName(builder.getDescription());
            final ContextNames.BindInfo bindingInfo = ContextNames.bindInfoFor(builder.getJNDIName());

            if (builder.getModuleName() != null) {
                // maintain a mapping from JNDI name to NoSQL module name, that we will use during deployment time to
                // identify the static module name to add to the deployment.
                jndiNameToModuleName.put(builder.getJNDIName(), builder.getModuleName());
            }
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
            // add service dependency on each separate hostname/port reference in standalone*.xml referenced from this driver profile definition.
            for (final String outboundSocketBinding : outboundSocketBindings) {
                final ServiceName outboundSocketBindingDependency = context.getCapabilityServiceName(CassandraDriverDefinition.OUTBOUND_SOCKET_BINDING_CAPABILITY_NAME, outboundSocketBinding, OutboundSocketBinding.class);
                serviceBuilder.addDependency(ServiceBuilder.DependencyType.REQUIRED, outboundSocketBindingDependency, OutboundSocketBinding.class, cassandraClientConnectionsService.getOutboundSocketBindingInjector(outboundSocketBinding));
            }
            serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE).install();
        }
    }

}
