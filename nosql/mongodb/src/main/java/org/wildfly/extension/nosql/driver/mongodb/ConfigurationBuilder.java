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

package org.wildfly.extension.nosql.driver.mongodb;

/**
 * ConfigurationBuilder
 *
 * @author Scott Marlow
 */
public class ConfigurationBuilder {

    private String JNDIName;
    private String database;
    private String description;
    private static final String defaultModuleName = "org.mongodb.driver";
    private String moduleName = // name of MongoDB module
            defaultModuleName;
    private String writeConcern;
    private String securityDomain;

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
}
