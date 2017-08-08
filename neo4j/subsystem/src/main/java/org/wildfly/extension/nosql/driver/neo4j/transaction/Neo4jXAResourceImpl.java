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

import static org.wildfly.nosql.common.NoSQLLogger.ROOT_LOGGER;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.LastResource;
import org.jboss.tm.XAResourceWrapper;

/**
 * Local Neo4j XA resource implementation, forked from IronJacamar project
 *
 * @author <a href="mailto:gurkanerdogdu@yahoo.com">Gurkan Erdogdu</a>
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @author Scott Marlow
 */
public class Neo4jXAResourceImpl implements LastResource, XAResourceWrapper {


    private static final boolean trace = ROOT_LOGGER.isTraceEnabled();

    /**
     * Current transaction branch id
     */
    private Xid currentXid;

    /**
     * Product name
     */
    private String productName;

    /**
     * Product version
     */
    private String productVersion;

    /**
     * Product version
     */
    private String jndiName;

    private Object underlyingTransaction;

    private TransactionControl transactionControl;

    public Neo4jXAResourceImpl(TransactionControl transactionControl, String jndiName, String productName, String productVersion) {
        this.transactionControl = transactionControl;
        this.jndiName = jndiName;
        this.productName = productName;
        this.productVersion = productVersion;
    }

    /**
     * {@inheritDoc}
     */
    public void start(Xid xid, int flags) throws XAException {
        if (trace)
            ROOT_LOGGER.tracef("start(%s, %s)", xid, flags);

        if (currentXid != null && flags == XAResource.TMNOFLAGS) {
            throw new LocalXAException("Trying to start a new transaction when old is not complete: Old: "
                    + currentXid + ", New " + xid + ", Flags " + flags, XAException.XAER_PROTO);
        }

        if (currentXid == null && flags != XAResource.TMNOFLAGS) {
            throw new LocalXAException("Trying to start a new transaction with wrong flags: New " + xid + ", Flags " + flags,
                    XAException.XAER_PROTO);
        }

        if (currentXid == null) {
            try {
                this.underlyingTransaction = transactionControl.beginTransaction();
                // cl.getManagedConnection().getLocalTransaction().begin();
            } catch (Throwable t) {
                throw new LocalXAException("Throwable trying to start local transaction", XAException.XAER_RMERR, t);
            }
            currentXid = xid;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void commit(Xid xid, boolean onePhase) throws XAException {
        if (!xid.equals(currentXid)) {
            throw new XAException(XAException.XAER_PROTO);
        }
        currentXid = null;

        // cl.getManagedConnection().getLocalTransaction().commit();
        try {
            transactionControl.success();
        } finally {
            transactionControl.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void rollback(Xid xid) throws XAException {
        if (!xid.equals(currentXid)) {
            throw new XAException(XAException.XAER_PROTO);
        }
        currentXid = null;
        // cl.getManagedConnection().getLocalTransaction().rollback();
        try {
            transactionControl.failure();
        } finally {
            transactionControl.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void end(Xid xid, int flags) throws XAException {
        if (trace) {
            ROOT_LOGGER.tracef("end(%s,%s)", xid, flags);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void forget(Xid xid) throws XAException {
        throw new XAException(XAException.XAER_RMERR);
    }

    /**
     * {@inheritDoc}
     */
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return xaResource == this;
    }

    /**
     * {@inheritDoc}
     */
    public int prepare(Xid xid) throws XAException {
        //if (!warned)
        //{
        //    ROOT_LOGGER.prepareCalledOnLocaltx();
        //}
        //warned = true;
        return XAResource.XA_OK;
    }

    /**
     * {@inheritDoc}
     */
    public Xid[] recover(int flag) throws XAException {
        throw new XAException(XAException.XAER_RMERR);
    }

    /**
     * {@inheritDoc}
     */
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public XAResource getResource() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String getProductName() {
        return productName;
    }

    /**
     * {@inheritDoc}
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * {@inheritDoc}
     */
    public String getJndiName() {
        return jndiName;
    }

}
