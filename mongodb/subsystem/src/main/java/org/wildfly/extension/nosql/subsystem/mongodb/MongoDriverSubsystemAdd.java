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
