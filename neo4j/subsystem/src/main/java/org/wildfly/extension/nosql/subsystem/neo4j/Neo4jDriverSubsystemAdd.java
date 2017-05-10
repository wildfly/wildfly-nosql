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

package org.wildfly.extension.nosql.subsystem.neo4j;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.operations.validation.ParametersValidator;
import org.jboss.as.txn.service.TransactionManagerService;
import org.jboss.as.txn.service.TransactionSynchronizationRegistryService;
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
                final int PARSE_NEO4J_DRIVER                          = 0x4C00;
                final int DEPENDENCIES_NEO4J_DRIVER                   = 0x1F10;
                // TODO: use Phase.PARSE_NEO4J_DRIVER
                processorTarget.addDeploymentProcessor(Neo4jDriverExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_NEO4J_DRIVER, new DriverScanDependencyProcessor("neo4jsubsystem"));
                // TODO: use Phase.DEPENDENCIES_NEO4J_DRIVER
                processorTarget.addDeploymentProcessor(Neo4jDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, DEPENDENCIES_NEO4J_DRIVER, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        startNeo4jDriverSubsysteService(context);

    }

    private void startNeo4jDriverSubsysteService(final OperationContext context) {
        Neo4jSubsystemService neo4jSubsystemService = new Neo4jSubsystemService();
        context.getServiceTarget().addService(Neo4jSubsystemService.serviceName(), neo4jSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE)
        .addDependency(TransactionManagerService.SERVICE_NAME, TransactionManager.class, neo4jSubsystemService.getTransactionManagerInjector())
        .addDependency(TransactionSynchronizationRegistryService.SERVICE_NAME, TransactionSynchronizationRegistry.class, neo4jSubsystemService.getTxSyncRegistryInjector())
                .install();
    }


}
