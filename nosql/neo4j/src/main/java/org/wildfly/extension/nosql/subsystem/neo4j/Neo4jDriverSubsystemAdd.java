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
                // TODO: create Phase.PARSE_NEO4J_DRIVER to use instead of phase.PARSE_PERSISTENCE_UNIT + 4 hack
                processorTarget.addDeploymentProcessor(Neo4jDriverExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_PERSISTENCE_UNIT + 4, new DriverScanDependencyProcessor("neo4jsubsystem"));
                // TODO: create Phase.DEPENDENCIES_NEO4J_DRIVER to use instead of phase.PARSE_PERSISTENCE_UNIT + 4 hack
                processorTarget.addDeploymentProcessor(Neo4jDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, Phase.DEPENDENCIES_PERSISTENCE_ANNOTATION + 4, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        startNeo4jDriverSubsysteService(context);

    }

    private void startNeo4jDriverSubsysteService(final OperationContext context) {
        Neo4jSubsystemService neo4jSubsystemService = new Neo4jSubsystemService();
        context.getServiceTarget().addService(Neo4jSubsystemService.serviceName(), neo4jSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }


}
