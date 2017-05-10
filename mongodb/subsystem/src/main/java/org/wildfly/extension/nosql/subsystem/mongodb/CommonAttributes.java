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

package org.wildfly.extension.nosql.subsystem.mongodb;

/**
 * CommonAttributes
 *
 * @author Scott Marlow
 */
public interface CommonAttributes {
    String DATABASE = "database";
    String OUTBOUND_SOCKET_BINDING_REF = "outbound-socket-binding-ref";
    String HOST_DEF = "host";
    String ID_NAME = "id";
    String JNDI_NAME = "jndi-name";
    String MODULE_NAME = "module";
    String PROFILE = "mongo";
    String PROPERTIES = "properties";
    String PROPERTY = "property";
    String WRITE_CONCERN = "writeConcern";
    String READ_CONCERN = "readConcern";
    String SECURITY_DOMAIN = "security-domain";
    String AUTH_TYPE = "auth-type";
    String SSL = "ssl";
}
