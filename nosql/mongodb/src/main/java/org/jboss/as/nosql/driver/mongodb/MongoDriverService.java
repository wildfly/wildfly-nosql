/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.jboss.as.nosql.driver.mongodb;

import static org.jboss.as.nosql.subsystem.common.NoSQLLogger.ROOT_LOGGER;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.jboss.as.network.OutboundSocketBinding;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.inject.MapInjector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * MongoDriverService represents the connections into a MongoDB server
 *
 * @author Scott Marlow
 */
public class MongoDriverService implements Service<MongoDriverService> {
    final ConfigurationBuilder configurationBuilder;
    // standard application server way to obtain target hostname + port for target NoSQL database server(s)
    private Map<String, OutboundSocketBinding> outboundSocketBindings = new HashMap<String, OutboundSocketBinding>();
    private MongoClient client;
    private MongoDatabase database;
    private MongoInteraction mongoInteraction;

    public MongoDriverService(ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
        mongoInteraction = new MongoInteraction(configurationBuilder);
    }

    public Injector<OutboundSocketBinding> getOutboundSocketBindingInjector(String name) {
        return new MapInjector<String, OutboundSocketBinding>(outboundSocketBindings, name);
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        for (OutboundSocketBinding target : outboundSocketBindings.values()) {
            mongoInteraction.hostPort(target.getUnresolvedDestinationAddress(), target.getDestinationPort());
        }
        client = mongoInteraction.mongoClient();
        if (configurationBuilder.getDatabase() != null) {
            database = mongoInteraction.getDB(client);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        try {
            mongoInteraction.close(client);
        } catch (Throwable throwable) {
            ROOT_LOGGER.driverFailedToStop(throwable);
        }
        client = null;
    }

    @Override
    public MongoDriverService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    public Object getClient() {
        return client;
    }

    public Object getDatabase() {
        return database;
    }

}
