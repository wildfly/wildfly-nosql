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


import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.wildfly.nosql.common.DriverDependencyProcessor;
import org.wildfly.nosql.common.DriverScanDependencyProcessor;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

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

        startCassandraDriverSubsysteService(context);
    }

    private void startCassandraDriverSubsysteService(final OperationContext context) {
        CassandraSubsystemService cassandraSubsystemService = new CassandraSubsystemService();
        context.getServiceTarget().addService(CassandraSubsystemService.serviceName(), cassandraSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }


}
