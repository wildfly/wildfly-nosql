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
