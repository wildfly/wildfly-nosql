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

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.wildfly.nosql.common.DriverDependencyProcessor;
import org.wildfly.nosql.common.DriverScanDependencyProcessor;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
final class OrientDriverSubsystemAdd extends AbstractBoottimeAddStepHandler {

    static final OrientDriverSubsystemAdd INSTANCE = new OrientDriverSubsystemAdd();

    private final ParametersValidator runtimeValidator = new ParametersValidator();

    private OrientDriverSubsystemAdd() {
        super(OrientDriverDefinition.DRIVER_SERVICE_CAPABILITY);
    }

    protected void performBoottime(final OperationContext context, final ModelNode operation, final ModelNode model)
            throws OperationFailedException {
        runtimeValidator.validate(operation.resolve());
        context.addStep(new AbstractDeploymentChainStep() {
            protected void execute(DeploymentProcessorTarget processorTarget) {
                // TODO: create Phase.PARSE_ORIENT_DRIVER
                processorTarget.addDeploymentProcessor(OrientDriverExtension.SUBSYSTEM_NAME, Phase.PARSE,
                        Phase.PARSE_PERSISTENCE_UNIT + 5, new DriverScanDependencyProcessor("orientdbsubsystem"));
                // TODO: create Phase.DEPENDENCIES_ORIENT_DRIVER
                processorTarget.addDeploymentProcessor(OrientDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES,
                        Phase.DEPENDENCIES_PERSISTENCE_ANNOTATION + 5, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        startOrientSubsystemService(context);
    }

    private void startOrientSubsystemService(final OperationContext context) {
        OrientSubsystemService neo4jSubsystemService = new OrientSubsystemService();
        context.getServiceTarget().addService(OrientSubsystemService.SERVICE_NAME, neo4jSubsystemService)
                .setInitialMode(ServiceController.Mode.ACTIVE).install();
    }

}
