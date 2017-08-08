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

package org.wildfly.extension.nosql.driver.neo4j.transaction;

import javax.transaction.xa.XAException;

/**
 * LocalXAException
 *
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @author Scott Marlow
 */
public class LocalXAException extends XAException {
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 158979393952L;

    /**
     * Creates a new instance.
     *
     * @param message   message
     * @param errorcode error code
     */
    public LocalXAException(String message, int errorcode) {
        this(message, errorcode, null);
    }

    /**
     * Creates a new instance.
     *
     * @param message   message
     * @param t         cause
     * @param errorcode error code
     */
    public LocalXAException(String message, int errorcode, Throwable t) {
        super(message);
        this.errorCode = errorcode;
        initCause(t);
    }

}
