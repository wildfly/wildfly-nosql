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

package org.wildfly.extension.nosql.subsystem.orientdb;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.nosql.common.SubsystemService;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class OrientSubsystemService implements Service<SubsystemService>, SubsystemService {

    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("orientdbsubsystem");
    private static final String VENDORKEY = "OrientDB";

    private final Map<String, String> jndiNameToModuleName = new ConcurrentHashMap<>();

    private final Map<String, String> profileNameToModuleName = new ConcurrentHashMap<>();

    @Override
    public void start(StartContext startContext) throws StartException {

    }

    @Override
    public void stop(StopContext stopContext) {

    }

    @Override
    public SubsystemService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public String moduleNameFromJndi(String jndiName) {
        return jndiNameToModuleName.get(jndiName);
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


}
