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

package org.wildfly.extension.nosql.driver.neo4j.transaction;

/**
 * TransactionControl is a private api for controlling the Neo4j inte
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
