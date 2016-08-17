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

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resources;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Named;

import org.jboss.as.ee.weld.WeldDeploymentMarker;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.weld.deployment.WeldPortableExtensions;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceName;

/**
 * DriverScanDependencyProcessor
 *
 * @author Scott Marlow
 */
public class DriverScanDependencyProcessor implements DeploymentUnitProcessor {

    private static final DotName RESOURCE_ANNOTATION_NAME = DotName.createSimple(Resource.class.getName());
    private static final DotName RESOURCES_ANNOTATION_NAME = DotName.createSimple(Resources.class.getName());
    private static final DotName NAMED_ANNOTATION_NAME = DotName.createSimple(Named.class.getName());
    // there should be no more than one NoSQL module referenced (there can be many references to that module but only
    // one version of NoSQL should be included per application deployment).
    private static final AttachmentKey<String> perModuleNameKey = AttachmentKey.create(String.class);

    private ServiceName serviceName;

    public DriverScanDependencyProcessor(String serviceName) {
        this.serviceName = ServiceName.JBOSS.append(serviceName);
    }

    /**
     * Add dependencies for modules required for NoSQL deployments
     */
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final CompositeIndex index = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);

        // handle @Resource
        final List<AnnotationInstance> resourceAnnotations = index.getAnnotations(RESOURCE_ANNOTATION_NAME);
        for (AnnotationInstance annotation : resourceAnnotations) {
            final AnnotationTarget annotationTarget = annotation.target();
            final AnnotationValue lookupValue = annotation.value("lookup");
            final String lookup = lookupValue != null ? lookupValue.asString() : null;
            if (lookup != null) {
                if (annotationTarget instanceof FieldInfo) {
                    processFieldResource(deploymentUnit, lookup);
                } else if (annotationTarget instanceof MethodInfo) {
                    final MethodInfo methodInfo = (MethodInfo) annotationTarget;
                    processMethodResource(deploymentUnit, methodInfo, lookup);
                } else if (annotationTarget instanceof ClassInfo) {
                    processClassResource(deploymentUnit, lookup);
                }
            }
        }

        // handle @Resources
        final List<AnnotationInstance> resourcesAnnotations = index.getAnnotations(RESOURCES_ANNOTATION_NAME);
        for (AnnotationInstance outerAnnotation : resourcesAnnotations) {
            final AnnotationTarget annotationTarget = outerAnnotation.target();
            if (annotationTarget instanceof ClassInfo) {
                final AnnotationInstance[] values = outerAnnotation.value("value").asNestedArray();
                for (AnnotationInstance annotation : values) {
                    final AnnotationValue lookupValue = annotation.value("lookup");
                    final String lookup = lookupValue != null ? lookupValue.asString() : null;
                    if (lookup != null) {
                        processClassResource(deploymentUnit, lookup);
                    }
                }
            }
        }

        // handle CDI @Named for @Inject, look for any @Named value that matches a NoSQL profile name
        final List<AnnotationInstance> namedAnnotations = index.getAnnotations(NAMED_ANNOTATION_NAME);
        for (AnnotationInstance annotation : namedAnnotations) {
            final AnnotationTarget annotationTarget = annotation.target();
            final AnnotationValue profileValue = annotation.value("value");
            final String profile = profileValue != null ? profileValue.asString() : null;

            if (annotationTarget instanceof FieldInfo) {
                processFieldNamedQualifier(deploymentUnit, profile);
            } else if (annotationTarget instanceof MethodInfo) {
                final MethodInfo methodInfo = (MethodInfo) annotationTarget;
                processMethodNamedQualifier(deploymentUnit, methodInfo, profile);
            } else if (annotationTarget instanceof ClassInfo) {
                processClassNamedQualifier(deploymentUnit, profile);
            }
        }
    }

    private void processClassNamedQualifier(DeploymentUnit deploymentUnit, String profile) {
        if (isEmpty(profile)) {
            throw ROOT_LOGGER.annotationAttributeMissing("@Named", "value");
        }
        String moduleName = getService().moduleNameFromProfile(profile);
        if (moduleName != null) {
            savePerDeploymentModuleName(deploymentUnit, moduleName);
        }
    }

    private void processMethodNamedQualifier(DeploymentUnit deploymentUnit, MethodInfo methodInfo, String profile) {
        final String methodName = methodInfo.name();
        if (!methodName.startsWith("set") || methodInfo.args().length != 1) {
            throw ROOT_LOGGER.setterMethodOnly("@Named", methodInfo);
        }
        String moduleName = getService().moduleNameFromProfile(profile);
        if (moduleName != null) {
            savePerDeploymentModuleName(deploymentUnit, moduleName);
        }
    }

    private void processFieldNamedQualifier(DeploymentUnit deploymentUnit, String profile) {
        String moduleName = getService().moduleNameFromProfile(profile);
        if (moduleName != null) {
            savePerDeploymentModuleName(deploymentUnit, moduleName);
        }
    }

    protected void processFieldResource(final DeploymentUnit deploymentUnit, String lookup) throws DeploymentUnitProcessingException {

        String moduleName = getService().moduleNameFromJndi(lookup);
        if (moduleName != null) {
            savePerDeploymentModuleName(deploymentUnit, moduleName);
        }
    }

    protected void processMethodResource(final DeploymentUnit deploymentUnit, final MethodInfo methodInfo, final String lookup) throws DeploymentUnitProcessingException {
        final String methodName = methodInfo.name();
        if (!methodName.startsWith("set") || methodInfo.args().length != 1) {
            throw ROOT_LOGGER.setterMethodOnly("@Resource", methodInfo);
        }
        String moduleName = getService().moduleNameFromJndi(lookup);
        if (moduleName != null) {
            savePerDeploymentModuleName(deploymentUnit, moduleName);
        }
    }

    protected void processClassResource(final DeploymentUnit deploymentUnit, final String lookup) throws DeploymentUnitProcessingException {
        if (isEmpty(lookup)) {
            throw ROOT_LOGGER.annotationAttributeMissing("@Resource", "lookup");
        }
        String moduleName = getService().moduleNameFromJndi(lookup);
        if (moduleName != null) {
            savePerDeploymentModuleName(deploymentUnit, moduleName);
        }
    }

    private void savePerDeploymentModuleName(DeploymentUnit deploymentUnit, String module) {
        if (deploymentUnit.getParent() != null) {
            deploymentUnit = deploymentUnit.getParent();
        }
        synchronized (deploymentUnit) {
            String currentValue = deploymentUnit.getAttachment(perModuleNameKey);
            // saved if not already set by another thread
            if (currentValue == null) {
                deploymentUnit.putAttachment(perModuleNameKey, module);
            } else if (!module.equals(currentValue)) {
                throw ROOT_LOGGER.cannotAddReferenceToModule(module, currentValue, deploymentUnit.getName());
            }
        }

        // register CDI extensions for each NoSQL driver that is used by deployment
        final ModuleLoader moduleLoader = Module.getBootModuleLoader();
        mongoSetup(deploymentUnit, moduleLoader, module);
        cassandraSetup(deploymentUnit, moduleLoader, module);
        neo4jSetup(deploymentUnit, moduleLoader, module);
        orientSetup(deploymentUnit, moduleLoader, module);
    }

    protected static String getPerDeploymentDeploymentModuleName(DeploymentUnit deploymentUnit) {
        if (deploymentUnit.getParent() != null) {
            deploymentUnit = deploymentUnit.getParent();
        }
        synchronized (deploymentUnit) {
            return deploymentUnit.getAttachment(perModuleNameKey);
        }
    }

    private void mongoSetup(DeploymentUnit deploymentUnit, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        Class mongoClientClass, mongoDatabaseClass;
        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();
        try {
            mongoClientClass = moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.MONGOCLIENTCLASS);
            mongoDatabaseClass = moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.MONGODATABASECLASS);

        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a MongoDB module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a MongoDB driver
        try {
            final DeploymentUnit parent = deploymentUnit.getParent() == null ? deploymentUnit : deploymentUnit.getParent();
            if (WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
                WeldPortableExtensions extensions = WeldPortableExtensions.getPortableExtensions(parent);
                ModuleIdentifier mongoCDIExtensionModule = ModuleIdentifier.create(NoSQLConstants.MONGOCDIEXTENSIONMODULE);
                methodHandleBuilder.classLoader(mongoCDIExtensionModule);
                methodHandleBuilder.className(NoSQLConstants.MONGOCDIEXTENSIONCLASS);
                MethodHandle extensionCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, Class.class, Class.class));

                Extension extension = (Extension) extensionCtor.invoke(mongoClientClass, mongoDatabaseClass);
                extensions.registerExtensionInstance(extension, parent);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("unexpected error constructing " + methodHandleBuilder.getTargetClass().getName(), throwable);
        }
    }

    private void orientSetup(DeploymentUnit deploymentUnit, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        Class oPartitionedDatabasePoolClass;
        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();
        try {
            oPartitionedDatabasePoolClass = moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.ORIENTDBPARTIONEDDBPOOLCLASS);
        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a OrientDB module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a Orient driver
        try {
            final DeploymentUnit parent = deploymentUnit.getParent() == null ? deploymentUnit : deploymentUnit.getParent();
            if (WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
                WeldPortableExtensions extensions = WeldPortableExtensions.getPortableExtensions(parent);
                ModuleIdentifier cdiExtensionModule = ModuleIdentifier.create(NoSQLConstants.ORIENTDBCDIEXTENSIONMODULE);
                methodHandleBuilder.classLoader(cdiExtensionModule);
                methodHandleBuilder.className(NoSQLConstants.ORIENTCDIEXTENSIONCLASS);
                MethodHandle extensionCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, Class.class));

                Extension extension = (Extension) extensionCtor.invoke(oPartitionedDatabasePoolClass);
                extensions.registerExtensionInstance(extension, parent);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("unexpected error constructing " + methodHandleBuilder.getTargetClass().getName(), throwable);
        }
    }

    private void neo4jSetup(DeploymentUnit deploymentUnit, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        Class driverClass;
        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();
        try {
            driverClass = moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.NEO4JDRIVERCLASS);
        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a Neo4j module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a Neo4j driver
        try {
            final DeploymentUnit parent = deploymentUnit.getParent() == null ? deploymentUnit : deploymentUnit.getParent();
            if (WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
                WeldPortableExtensions extensions = WeldPortableExtensions.getPortableExtensions(parent);
                ModuleIdentifier cdiExtensionModule = ModuleIdentifier.create(NoSQLConstants.NEO4JCDIEXTENSIONMODULE);
                methodHandleBuilder.classLoader(cdiExtensionModule);
                methodHandleBuilder.className(NoSQLConstants.NEO4JCDIEXTENSIONCLASS);
                MethodHandle extensionCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, Class.class));

                Extension extension = (Extension) extensionCtor.invoke(driverClass);
                extensions.registerExtensionInstance(extension, parent);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("unexpected error constructing " + methodHandleBuilder.getTargetClass().getName(), throwable);
        }
    }

    private void cassandraSetup(DeploymentUnit deploymentUnit, ModuleLoader moduleLoader, String nosqlDriverModuleName) {
        Class clusterClass;
        Class sessionClass;

        MethodHandleBuilder methodHandleBuilder = new MethodHandleBuilder();
        try {
            clusterClass = moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.CASSANDRACLUSTERCLASS);
            sessionClass = moduleLoader.loadModule(ModuleIdentifier.fromString(nosqlDriverModuleName)).getClassLoader().loadClass(NoSQLConstants.CASSANDRASESSIONCLASS);
        } catch (ClassNotFoundException expected) {
            // ignore CNFE which just means that module is not a Cassandra module
            return;
        } catch (ModuleLoadException e) {
            throw new RuntimeException("could not load NoSQL driver module " + nosqlDriverModuleName, e);
        }
        // only reach this point if module is a Cassandra driver
        try {
            final DeploymentUnit parent = deploymentUnit.getParent() == null ? deploymentUnit : deploymentUnit.getParent();
            if (WeldDeploymentMarker.isPartOfWeldDeployment(deploymentUnit)) {
                WeldPortableExtensions extensions = WeldPortableExtensions.getPortableExtensions(parent);
                ModuleIdentifier cdiExtensionModule = ModuleIdentifier.create(NoSQLConstants.CASSANDRACDIEXTENSIONMODULE);
                methodHandleBuilder.classLoader(cdiExtensionModule);
                methodHandleBuilder.className(NoSQLConstants.CASSANDRACDIEXTENSIONCLASS);
                MethodHandle extensionCtor = methodHandleBuilder.constructor(MethodType.methodType(void.class, Class.class, Class.class));

                Extension extension = (Extension) extensionCtor.invoke(clusterClass, sessionClass);
                extensions.registerExtensionInstance(extension, parent);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("unexpected error constructing " + methodHandleBuilder.getTargetClass().getName(), throwable);
        }

    }

    private SubsystemService getService() {
        return (SubsystemService) CurrentServiceContainer.getServiceContainer().getService(serviceName).getValue();
    }


    private boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    @Override
    public void undeploy(DeploymentUnit context) {

    }
}
