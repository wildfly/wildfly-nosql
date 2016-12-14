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

package org.jboss.as.test.compat.nosql.mongodb.compensations;

import javax.inject.Inject;

import org.bson.Document;
import org.jboss.narayana.compensations.api.CompensationManager;
import org.jboss.narayana.compensations.api.TxCompensate;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author <a href="mailto:paul.robinson@redhat.com">Paul Robinson</a>
 */
public class AccountManager {

    @Inject
    private CompensationManager compensationManager;

    @Inject
    private CreditData creditData;

    @Inject
    private DebitData debitData;

    @Inject
    private AccountDao accountDao;

    @TxCompensate(UndoCredit.class)
    public void creditAccount(String account, int amount) {
        if (amount > 500) {
            compensationManager.setCompensateOnly();
            return;
        }
        creditData.setAccount(account);
        creditData.setAmount(amount);
        accountDao.update(account, new Document("$inc", new Document("balance", amount)));
    }

    @TxCompensate(UndoDebit.class)
    public void debitAccount(String account, int amount) {
        debitData.setAccount(account);
        debitData.setAmount(amount);
        accountDao.update(account, new Document("$inc", new Document("balance", -1 * amount)));
    }

}
