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

package org.wildfly.nosql.common;

import java.util.Map;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
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
        final Map<String, String> nosqlDriverModuleNameMap = DriverScanDependencyProcessor.getPerDeploymentDeploymentModuleName(deploymentUnit);
        if (nosqlDriverModuleNameMap == null) {
            return;
        }
        for (String nosqlDriverModuleName : nosqlDriverModuleNameMap.values()) {
            if (nosqlDriverModuleName != null) {
                final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
                final ModuleLoader moduleLoader = Module.getBootModuleLoader();
                addDependency(moduleSpecification, moduleLoader, ModuleIdentifier.fromString(nosqlDriverModuleName));
                addMongoCDIDependency(moduleSpecification, moduleLoader, nosqlDriverModuleName);
                addCassandraCDIDependency(moduleSpecification, moduleLoader, nosqlDriverModuleName);
                addNeo4jCDIDependency(moduleSpecification, moduleLoader, nosqlDriverModuleName);
                addOrientCDIDependency(moduleSpecification, moduleLoader, nosqlDriverModuleName);
            }
        }
    }

    private void addMongoCDIDependency(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        try {
            moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.MONGOCLIENTCLASS);
        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a MongoDB module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a MongoDB driver
        ModuleIdentifier mongoCDIExtensionModule = ModuleIdentifier.create(NoSQLConstants.MONGOCDIEXTENSIONMODULE);
        addDependency(moduleSpecification, moduleLoader, mongoCDIExtensionModule);
    }

    private void addNeo4jCDIDependency(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        try {
            moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.NEO4JDRIVERCLASS);
        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a Neo4j module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a Neo4j driver
        ModuleIdentifier mongoCDIExtensionModule = ModuleIdentifier.create(NoSQLConstants.NEO4JCDIEXTENSIONMODULE);
        addDependency(moduleSpecification, moduleLoader, mongoCDIExtensionModule);
    }

    private void addCassandraCDIDependency(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        try {
            moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.CASSANDRACLUSTERCLASS);
        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a Cassandra module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a Cassandra driver
        ModuleIdentifier mongoCDIExtensionModule = ModuleIdentifier.create(NoSQLConstants.CASSANDRACDIEXTENSIONMODULE);
        addDependency(moduleSpecification, moduleLoader, mongoCDIExtensionModule);
    }

    private void addOrientCDIDependency(ModuleSpecification moduleSpecification, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        try {
            moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.ORIENTDBPARTIONEDDBPOOLCLASS);
        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a OrientDB module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a OrientDB driver
        ModuleIdentifier mongoCDIExtensionModule = ModuleIdentifier.create(NoSQLConstants.ORIENTDBCDIEXTENSIONMODULE);
        addDependency(moduleSpecification, moduleLoader, mongoCDIExtensionModule);
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
