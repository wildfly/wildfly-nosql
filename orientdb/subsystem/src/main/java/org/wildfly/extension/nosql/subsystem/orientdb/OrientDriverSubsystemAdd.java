/*
 * *
 *  * Copyright 2017 Red Hat, Inc, and individual contributors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
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
                final int PARSE_ORIENT_DRIVER                         = 0x4C01;
                final int DEPENDENCIES_ORIENT_DRIVER                  = 0x1F11;
                // TODO: use Phase.PARSE_ORIENT_DRIVER
                processorTarget.addDeploymentProcessor(OrientDriverExtension.SUBSYSTEM_NAME, Phase.PARSE,
                        PARSE_ORIENT_DRIVER, new DriverScanDependencyProcessor("orientdbsubsystem"));
                // TODO: use Phase.DEPENDENCIES_ORIENT_DRIVER
                processorTarget.addDeploymentProcessor(OrientDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES,
                        DEPENDENCIES_ORIENT_DRIVER, DriverDependencyProcessor.getInstance());
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
