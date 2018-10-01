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
                final int PARSE_CASSANDRA_DRIVER                      = 0x4C02;
                final int DEPENDENCIES_CASSANDRA_DRIVER               = 0x1F12;
                // TODO: use Phase.PARSE_CASSANDRA_DRIVER
                processorTarget.addDeploymentProcessor(CassandraDriverExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_CASSANDRA_DRIVER, new DriverScanDependencyProcessor("cassandrasubsystem"));
                // TODO: use Phase.DEPENDENCIES_CASSANDRA_DRIVER
                processorTarget.addDeploymentProcessor(CassandraDriverExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES, DEPENDENCIES_CASSANDRA_DRIVER, DriverDependencyProcessor.getInstance());
            }
        }, OperationContext.Stage.RUNTIME);

        startCassandraDriverSubsysteService(context);
    }

    private void startCassandraDriverSubsysteService(final OperationContext context) {
        CassandraSubsystemService cassandraSubsystemService = new CassandraSubsystemService();
        context.getServiceTarget().addService(CassandraSubsystemService.serviceName(), cassandraSubsystemService).setInitialMode(ServiceController.Mode.ACTIVE).install();
    }


}
