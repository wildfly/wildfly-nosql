/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.wildfly.extension.nosql.driver.mongodb.ReadConcernType;
import org.wildfly.extension.nosql.driver.mongodb.WriteConcernType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class PropertiesValidator implements ParameterValidator {

    private static final Map<String, List<String>> VALID_PROPERTIES = new HashMap<>();

    static {
        VALID_PROPERTIES.put(CommonAttributes.WRITE_CONCERN, WriteConcernType.NAMES);
        VALID_PROPERTIES.put(CommonAttributes.READ_CONCERN, ReadConcernType.NAMES);
    }

    @Override
    public void validateParameter(String parameterName, ModelNode values) throws OperationFailedException {
        for (Property property : values.asPropertyList()) {
            if (!VALID_PROPERTIES.containsKey(property.getName())) {
                throw new OperationFailedException("Invalid property name: " + property.getName());
            }

            if (!VALID_PROPERTIES.get(property.getName()).contains(property.getValue().asString().toUpperCase())) {
                throw new OperationFailedException(
                        "Invalid value of property " + property.getName() + ": " + property.getValue());
            }
        }
    }

    @Override
    public void validateResolvedParameter(String parameterName, ModelNode value) throws OperationFailedException {

    }

}
