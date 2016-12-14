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

package org.wildfly.extension.nosql.driver;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Configuration {

    private static final String DEFAULT_MODULE_NAME = "com.orientechnologies";

    private String databaseUrl = "";

    private String database = "";

    private String userName = "root";

    private String password = "";

    private int maxPartitionSize = 64;

    private int maxPoolSize = -1;

    private String jndiName = "";

    private String moduleName = DEFAULT_MODULE_NAME;

    private String profileName = "";

    private Configuration() {

    }

    private Configuration(Configuration configuration) {
        this.databaseUrl = configuration.getDatabaseUrl();
        this.database = configuration.getDatabase();
        this.userName = configuration.getUserName();
        this.password = configuration.getPassword();
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

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
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

        public Builder userName(String userName) {
            configuration.userName = userName;
            return this;
        }

        public Builder password(String password) {
            configuration.password = password;
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