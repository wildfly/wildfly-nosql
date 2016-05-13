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

import java.util.Map;

import org.wildfly.nosql.common.SubsystemService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * MongoSubsystemService represents the runtime aspects of the MongoDB client driver subsystem
 *
 * @author Scott Marlow
 */
public class MongoSubsystemService implements Service<SubsystemService>, SubsystemService {

    private static final ServiceName SERVICENAME = ServiceName.JBOSS.append("mongodbsubsystem");

    // JNDI name to module name for resolving the MongoDB module to inject into deployments
    private final Map<String, String> jndiNameToModuleName;

    public MongoSubsystemService(final Map<String, String> jndiNameToModuleName) {
        this.jndiNameToModuleName = jndiNameToModuleName;
    }

    public static ServiceName serviceName() {
        return SERVICENAME;
    }

    public String moduleName(String jndiName) {
        return jndiNameToModuleName.get(jndiName);
    }

    @Override
    public void start(StartContext context) throws StartException {

    }

    @Override
    public void stop(StopContext context) {

    }

    @Override
    public SubsystemService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

}
