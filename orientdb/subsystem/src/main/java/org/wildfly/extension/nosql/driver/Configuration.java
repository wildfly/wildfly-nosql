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

package org.wildfly.extension.nosql.driver;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Configuration {

    private static final String DEFAULT_MODULE_NAME = "com.orientechnologies";

    private String databaseUrl = "";

    private String database = "";

    private String securityDomain;

    private int maxPartitionSize = -1;  // let OrientDB driver determine defaults

    private int maxPoolSize = -1;       // let OrientDB driver determine defaults

    private String jndiName = "";

    private String moduleName = DEFAULT_MODULE_NAME;

    private String profileName = "";

    private Configuration() {

    }

    private Configuration(Configuration configuration) {
        this.databaseUrl = configuration.getDatabaseUrl();
        this.database = configuration.getDatabase();
        this.securityDomain = configuration.getSecurityDomain();
        this.maxPartitionSize = configuration.getMaxPartitionSize();
        this.maxPoolSize = configuration.getMaxPoolSize();
        this.jndiName = configuration.getJndiName();
        this.moduleName = configuration.getModuleName();
        this.profileName = configuration.getProfileName();
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabase() {
        return database;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }

    public int getMaxPartitionSize() {
        return maxPartitionSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public static class Builder {

        private Configuration configuration;

        public Builder() {
            configuration = new Configuration();
        }

        public Builder(Configuration configuration) {
            this.configuration = new Configuration(configuration);
        }

        public Builder databaseUrl(String databaseUrl) {
            configuration.databaseUrl = databaseUrl;
            return this;
        }

        public Builder database(String database) {
            configuration.database = database;
            return this;
        }

        public Builder securityDomain(String securityDomain) {
            configuration.securityDomain = securityDomain;
            return this;
        }

        public Builder maxPartitionSize(int maxPartitionSize) {
            configuration.maxPartitionSize = maxPartitionSize;
            return this;
        }

        public Builder maxPoolSize(int maxPoolSize) {
            configuration.maxPoolSize = maxPoolSize;
            return this;
        }

        public Builder jndiName(String jndiName) {
            configuration.jndiName = jndiName;
            return this;
        }

        public Builder moduleName(String moduleName) {
            configuration.moduleName = moduleName;
            return this;
        }

        public Builder profileName(String profileName) {
            configuration.profileName = profileName;
            return this;
        }

        public Configuration build() {
            return new Configuration(configuration);
        }

    }


}