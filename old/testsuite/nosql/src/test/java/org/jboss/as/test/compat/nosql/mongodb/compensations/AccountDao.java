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

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.annotation.Resource;

import java.util.Iterator;
import java.util.Optional;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class AccountDao {

    @Resource(lookup = "java:jboss/mongodb/test")
    private MongoDatabase database;

    public Optional<Document> get(String accountName) {
        Iterator<Document> result = getCollection().find(new Document("name", accountName)).iterator();
        if (result.hasNext()) {
            return Optional.of(result.next());
        }
        return Optional.empty();
    }

    public void update(String accountName, Document updatedAccount) {
        getCollection().updateOne(new Document("name", accountName), updatedAccount);
    }

    public void insert(Document account) {
        getCollection().insertOne(account);
    }

    public void clear() {
        getCollection().drop();
    }

    private MongoCollection<Document> getCollection() {
        return database.getCollection("accounts");
    }

}
