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

package org.wildfly.extension.nosql.driver.cassandra;

/**
 * ConfigurationBuilder
 *
 * @author Scott Marlow
 */
public class ConfigurationBuilder {
    private String description; //
    private String JNDIName;    // required global jndi name
    private String keyspace;    // optional Cassandra keyspace
    private String securityDomain;
    private boolean withSSL;

    private static final String defaultModuleName = "com.datastax.cassandra.driver-core";
    private String moduleName = // name of Cassandra static module
            defaultModuleName;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setJNDIName(String JNDIName) {
        this.JNDIName = JNDIName;
    }

    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getJNDIName() {
        return JNDIName;
    }

    public String getDescription() {
        return description;
    }

    public String getKeySpace() {
        return keyspace;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }

    public boolean isWithSSL() {
        return withSSL;
    }

    public void setWithSSL(boolean withSSL) {
        this.withSSL = withSSL;
    }

}
