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

import static org.jboss.logging.Logger.Level.ERROR;

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
    IllegalStateException cannotAddReferenceToModule(String module, String currentValue, String deploymentName);

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
}
