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

import java.util.ArrayList;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.jboss.msc.service.StartException;

/**
 * MongoInteraction
 *
 * @author Scott Marlow
 */
public class MongoInteraction {

    private ArrayList serverAddressArrayList = new ArrayList(); // List<ServerAddress>
    private ConfigurationBuilder configurationBuilder;

    public MongoInteraction(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }

    public void hostPort(String host, int port) throws StartException {
        if (port > 0) {
            serverAddressArrayList.add(new ServerAddress(host, port));
        } else {
            serverAddressArrayList.add(new ServerAddress(host));
        }
    }

    public MongoClient mongoClient() throws StartException {
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.description(configurationBuilder.getDescription());
        MongoClientOptions mongoClientOptions = builder.build();
        return new MongoClient(serverAddressArrayList, mongoClientOptions);
    }

    public MongoDatabase getDB(MongoClient client) throws StartException {
        return client.getDatabase(configurationBuilder.getDatabase());
    }


    public void close(MongoClient client) throws Throwable {
        if (client != null) {
            client.close();
        }
    }

}
