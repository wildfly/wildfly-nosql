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

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * Neo4jDriverExtension
 *
 * @author Scott Marlow
 */
public class Neo4jDriverExtension implements Extension {

    public static final String SUBSYSTEM_NAME = "neo4j";

    protected static final PathElement PROFILE_PATH = PathElement.pathElement(CommonAttributes.PROFILE);
    protected static final PathElement HOST_PATH = PathElement.pathElement(CommonAttributes.HOST_DEF);

    public static final String NAMESPACE = "urn:jboss:domain:neo4j:1.0";
    public static final Namespace CURRENT = Namespace.NEO4J_1_0;
    public static final String RESOURCE_NAME = Neo4jDriverExtension.class.getPackage().getName() + ".LocalDescriptions";

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String... keyPrefix) {
        StringBuilder prefix = new StringBuilder(SUBSYSTEM_NAME);
        for (String kp : keyPrefix) {
            prefix.append('.').append(kp);
        }
        return new StandardResourceDescriptionResolver(prefix.toString(), RESOURCE_NAME, Neo4jDriverExtension.class.getClassLoader(), true, false);
    }

    private static final Neo4jDriverSubsystemParser parser = Neo4jDriverSubsystemParser.INSTANCE;

    private static final ModelVersion CURRENT_MODEL_VERSION = ModelVersion.create(1, 0, 0);

    static ResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        return new StandardResourceDescriptionResolver(keyPrefix, RESOURCE_NAME, Neo4jDriverExtension.class.getClassLoader(), true, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, CURRENT_MODEL_VERSION);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(Neo4jDriverDefinition.INSTANCE);
        registration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
        subsystem.registerXMLElementWriter(parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    public static StandardResourceDescriptionResolver getResolver(final String... keyPrefix) {
        StringBuilder prefix = new StringBuilder(SUBSYSTEM_NAME);
        for (String kp : keyPrefix) {
            prefix.append('.').append(kp);
        }
        return new StandardResourceDescriptionResolver(prefix.toString(), RESOURCE_NAME, Neo4jDriverExtension.class.getClassLoader(), true, false);
    }
}
