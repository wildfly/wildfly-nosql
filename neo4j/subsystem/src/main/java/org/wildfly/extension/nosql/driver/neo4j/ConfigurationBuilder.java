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

package org.wildfly.extension.nosql.driver.neo4j;

import org.wildfly.extension.nosql.driver.neo4j.transaction.TransactionEnlistmentType;

/**
 * ConfigurationBuilder
 *
 * @author Scott Marlow
 */
public class ConfigurationBuilder {
    private String description; //
    private String JNDIName;    // required global jndi name

    private static final String defaultModuleName = "org.neo4j.driver";
    private String moduleName = // name of static module
            defaultModuleName;
    private TransactionEnlistmentType transactionEnlistment;
    private String securityDomain;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setJNDIName(String JNDIName) {
        this.JNDIName = JNDIName;
    }

    public String getJNDIName() {
        return JNDIName;
    }

    public String getDescription() {
        return description;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setTransactionEnlistment(TransactionEnlistmentType transactionEnlistment) {
        this.transactionEnlistment = transactionEnlistment;
    }

    public TransactionEnlistmentType getTransactionEnlistment() {
        return transactionEnlistment;
    }

    public void setSecurityDomain(String securityDomain) {
        this.securityDomain = securityDomain;
    }

    public String getSecurityDomain() {
        return securityDomain;
    }
}
