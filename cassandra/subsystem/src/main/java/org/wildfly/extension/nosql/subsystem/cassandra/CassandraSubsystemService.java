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

package org.wildfly.extension.nosql.subsystem.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.wildfly.nosql.common.SubsystemService;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * CassandraSubsystemService represents the runtime aspects of the Cassandra client driver subsystem
 *
 * @author Scott Marlow
 */
public class CassandraSubsystemService implements Service<SubsystemService>, SubsystemService {

    private static final String VENDORKEY = "Cassandra";

    private static final ServiceName SERVICENAME = ServiceName.JBOSS.append("cassandrasubsystem");

    // JNDI name to module name for resolving the Cassandra module to inject into deployments
    private final Map<String, String> jndiNameToModuleName = new ConcurrentHashMap<>();

    private final Map<String, String> profileNameToModuleName = new ConcurrentHashMap<>();

    public CassandraSubsystemService() {
    }

    public static ServiceName serviceName() {
        return SERVICENAME;
    }

    @Override
    public String moduleNameFromJndi(String jndiName) {
        return jndiNameToModuleName.get(jndiName);
    }

    public void addModuleNameFromJndi(String jndiName, String module) {
        jndiNameToModuleName.put(jndiName, module);
    }

    public void removeModuleNameFromJndi(String jndiName) {
        jndiNameToModuleName.remove(jndiName);
    }

    public void addModuleNameFromProfile(String profile, String moduleName) {
        profileNameToModuleName.put(profile, moduleName);
    }

    public void removeModuleNameFromProfile(String profile) {
        profileNameToModuleName.remove(profile);
    }

    @Override
    public String moduleNameFromProfile(String profileName) {
        return profileNameToModuleName.get(profileName);
    }

    @Override
    public Collection<String> profileNames() {
        return profileNameToModuleName.keySet();
    }

    @Override
    public Collection<String> jndiNames() {
        return jndiNameToModuleName.keySet();
    }

    @Override
    public String vendorKey() {
        return VENDORKEY;
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
