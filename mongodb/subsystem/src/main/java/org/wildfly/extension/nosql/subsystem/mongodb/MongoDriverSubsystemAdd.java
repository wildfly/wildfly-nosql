/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.extension.nosql.subsystem.mongodb;

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
 * MongoDriverSubsystemAdd
 *
 * @author Scott Marlow
 */
public class MongoDriverSubsystemAdd extends AbstractBoottimeAddStepHandler {

    public static final MongoDriverSubsystemAdd INSTANCE = new MongoDriverSubsystemAdd();

    private final ParametersValidator runtimeValidator = new ParametersValidator();

    private MongoDriverSubsystemAdd() {
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
                final int PARSE_MONGO_DRIVER                          = 0x4C03;
                final int DEPENDENCIES_MONGO_DRIVER                   = 0x1F13;
                // TODO: use Phase.PARSE_MONGO_DRIVER
                processorTarget.addDeploymentProcessor(MongoDriverExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_MONGO_DRIVER, new DriverScanDependencyProcessor("mongodbsubsystem"));
                // TODO: use Phase.DEPENDENCIES_MONGO_DRIVER
                processorTarget.addDeploymentProcessor(MongoDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, DEPENDENCIES_MONGO_DRIVER, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        startMongoDriverSubsysteService(context);
    }

    private void startMongoDriverSubsysteService(OperationContext context) {
        MongoSubsystemService mongoSubsystemService = new MongoSubsystemService();
        context.getServiceTarget().addService(MongoSubsystemService.serviceName(), mongoSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }


}
