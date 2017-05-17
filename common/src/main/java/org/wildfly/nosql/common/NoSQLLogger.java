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

package org.wildfly.nosql.common;

import static org.jboss.logging.Logger.Level.ERROR;

import java.util.ArrayList;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * NoSQLLogger
 *
 * @author Scott Marlow
 */
@MessageLogger(projectCode = "WFLYNOSQL", length = 4)
public interface NoSQLLogger extends BasicLogger {
    /**
     * Default root level logger with the package name for he category.
     */
    NoSQLLogger ROOT_LOGGER = Logger.getMessageLogger(NoSQLLogger.class, "org.wildfly.nosql");

    /**
     * Logs an error message indicating the driver failed to stop
     *
     * @param cause the cause of the error.
     */
    @LogMessage(level = ERROR)
    @Message(id = 1, value = "Failed to stop")
    void driverFailedToStop(@Cause Throwable cause);

    /**
     * Creates an exception
     * module to the deployment.
     *
     * @param module
     * @param currentValue
     * @param deploymentName
     * @return a {@link RuntimeException} for the error.
     */
    @Message(id = 2, value = "Cannot specify NoSQL module '%s' " +
            "when a different module '%s' is already associated with application (%s)")
    IllegalStateException cannotAddReferenceToModule(String module, Object currentValue, String deploymentName);

    /**
     * Creates an exception indicating the {@code annotation} injection target is invalid and only setter methods are
     * allowed.
     *
     * @param annotation the annotation.
     * @param methodInfo the method information.
     * @return an {@link IllegalArgumentException} for the error.
     */
    @Message(id = 3, value = "%s injection target is invalid.  Only setter methods are allowed: %s")
    IllegalArgumentException setterMethodOnly(String annotation, MethodInfo methodInfo);

    /**
     * Creates an exception indicating the annotation must provide the attribute.
     *
     * @param annotation the annotation.
     * @param attribute  the attribute.
     * @return an {@link IllegalArgumentException} for the exception.
     */
    @Message(id = 4, value = "%s annotations must provide a %s.")
    IllegalArgumentException annotationAttributeMissing(String annotation, String attribute);

    @Message(id = 5, value = "Cannot unwrap class '%s'.")
    IllegalArgumentException unassignable(Class clazz);

    @Message(id = 6, value = "Cannot set %s to %s.  Instead set to one of %s")
    OperationFailedException invalidParameter(String transaction, String str, ArrayList arrayList);
}
