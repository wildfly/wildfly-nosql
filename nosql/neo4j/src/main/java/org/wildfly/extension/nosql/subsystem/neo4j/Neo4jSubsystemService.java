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

import java.util.Collection;
import java.util.Map;

import org.wildfly.nosql.common.SubsystemService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * Neo4jSubsystemService represents the runtime aspects of the Neo4J client driver subsystem
 *
 * @author Scott Marlow
 */
public class Neo4jSubsystemService implements Service<SubsystemService>, SubsystemService {

    private static final ServiceName SERVICENAME = ServiceName.JBOSS.append("neo4jsubsystem");

    // JNDI name to module name for resolving the Ne04J module to inject into deployments
    private final Map<String, String> jndiNameToModuleName;

    private final Map<String, String> profileNameToModuleName;

    public Neo4jSubsystemService(final Map<String, String> jndiNameToModuleName, final Map<String, String> profileNameToModuleName) {
        this.jndiNameToModuleName = jndiNameToModuleName;
        this.profileNameToModuleName = profileNameToModuleName;
    }

    public static ServiceName serviceName() {
        return SERVICENAME;
    }

    @Override
    public String moduleNameFromJndi(String jndiName) {
        return jndiNameToModuleName.get(jndiName);
    }

    @Override
    public String moduleNameFromProfile(String profileName) {
        return profileNameToModuleName.get(profileName);
    }

    public Collection<String> profileNames() {
        return profileNameToModuleName.keySet();
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
