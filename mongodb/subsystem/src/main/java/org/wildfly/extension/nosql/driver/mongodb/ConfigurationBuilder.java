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

package org.wildfly.extension.nosql.driver.mongodb;

/**
 * ConfigurationBuilder
 *
 * @author Scott Marlow
 */
public class ConfigurationBuilder {

    private String JNDIName;
    private String database;
    private String adminDatabase;
    private String description;
    private static final String defaultModuleName = "org.mongodb.driver";
    private String moduleName = // name of MongoDB module
            defaultModuleName;
    private String writeConcern;
    private String readConcern;
    private String securityDomain;
    private AuthType authType;
    private boolean SSL;
    private String replicaSet;

    public AuthType getAuthType() {
        return authType;
    }

    public ConfigurationBuilder setAuthType(AuthType authType) {
        this.authType = authType;
        return this;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }

    public ConfigurationBuilder setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
        return this;
    }

    public ConfigurationBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ConfigurationBuilder setJNDIName(String JNDIName) {
        this.JNDIName = JNDIName;
        return this;
    }

    public String getJNDIName() {
        return JNDIName;
    }

    public String getDatabase() {
        return database;
    }

    public ConfigurationBuilder setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getAdminDatabase() {
        return adminDatabase;
    }

    public void setAdminDatabase(String adminDatabase) {
        this.adminDatabase = adminDatabase;
    }

    public ConfigurationBuilder setModuleName(String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getWriteConcern() {
        return writeConcern;
    }

    public ConfigurationBuilder setWriteConcern(String writeConcern) {
        this.writeConcern = writeConcern;
        return this;

    }

    public String getReadConcern() {
        return readConcern;
    }

    public ConfigurationBuilder setReadConcern(String readConcern) {
        this.readConcern = readConcern;
        return this;
    }

    public void setSSL(boolean SSL) {
        this.SSL = SSL;
    }

    public boolean isSSL() {
        return SSL;
    }

    public String getReplicaSet() {
        return replicaSet;
    }

    public void setReplicaSet(String replicaSet) {
        this.replicaSet = replicaSet;
    }

}
