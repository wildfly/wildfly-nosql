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

package org.wildfly.extension.nosql.driver.neo4j.transaction;

/**
 * TransactionControl is a private api for controlling the Neo4j transaction interaction
 *
 * @author Scott Marlow
 */
public interface TransactionControl {

    /**
     * beginTransaction is expected to be called when the transaction manager enlists the XAResource into the
     * transaction, in the XAResource.start(Xid xid, int flags) call.
     *
     * @return underlying transaction to the XAResource.start(Xid xid, int flag) implementation.
     */
    Object beginTransaction();

    /**
     * Expected to be called when transaction manager calls XAResource.commit(Xid xid, boolean onePhase).
     */
    void success();

    /**
     * Expected to be called when transaction manager calls XAResource.rollback(Xid xid).
     */
    void failure();

    /**
     * Expected to be called from both XAResource.rollback(Xid xid) + XAResource.commit(Xid xid, boolean onePhase).
     */
    void close();

}
