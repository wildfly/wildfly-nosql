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
