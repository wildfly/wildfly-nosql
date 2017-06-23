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

package org.wildfly.nosql.common;

import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceName;
import org.wildfly.nosql.common.spi.NoSQLConnection;

/**
 * ConnectionServiceAccess
 *
 * @author Scott Marlow
 */
public class ConnectionServiceAccess {

    private static final ServiceName SERVICENAME = ServiceName.JBOSS.append("NoSQLClientConnectionService");

    public static ServiceName serviceName(String id) {
        return SERVICENAME.append(id);
    }

    public static NoSQLConnection connection(String id) {
        return (NoSQLConnection) CurrentServiceContainer.getServiceContainer().getRequiredService(SERVICENAME.append(id)).getValue();
    }

}
