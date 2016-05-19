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

package org.wildfly.nosql.common;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

/**
 * DriverDependencyProcessor
 *
 * @author Scott Marlow
 */
public class DriverDependencyProcessor implements DeploymentUnitProcessor {

    private static final DriverDependencyProcessor driverDependencyProcessor = new DriverDependencyProcessor();

    public static DriverDependencyProcessor getInstance() {
        return driverDependencyProcessor;
    }

    /**
     * Add dependencies for modules required for NoSQL deployments
     */
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final String moduleName = DriverScanDependencyProcessor.getPerDeploymentDeploymentModuleName(deploymentUnit);

        if (moduleName != null) {
            final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
            final ModuleLoader moduleLoader = Module.getBootModuleLoader();

            addDependency(moduleSpecification, moduleLoader, ModuleIdentifier.create(moduleName));
            // TODO: remove temp hack:
            // (1) in place of hack, need to inject NoSQL driver module for @Inject
            // (2) also need to inject module containing CDI injection, so app classloader sees CDI extension
            if ("org.mongodb.driver".equals(moduleName)) // temp hack for cdi extension loading
                addDependency(moduleSpecification, moduleLoader, ModuleIdentifier.create("org.wildfly.extension.nosql.mongodb"));
        }

    }

    private void addDependency(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader,
                               ModuleIdentifier... moduleIdentifiers) {
        for (ModuleIdentifier moduleIdentifier : moduleIdentifiers) {
            moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, moduleIdentifier, false, false, true, false));
        }
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }
}
