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

package org.jboss.as.test.compat.nosql.orientdb.jaxrs;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jboss.as.test.compat.nosql.orientdb.TestPeopleDao;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Optional;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Path("/client")
@Stateless(name = "CustomName")
public class ClientResource {

    public static final String PERSON_PATH = "person";

    @Inject
    @Named("orientdbtestprofile")
    private OPartitionedDatabasePool databasePool;

    private TestPeopleDao peopleDao;

    @PostConstruct
    private void init() {
        peopleDao = new TestPeopleDao(databasePool);
    }

    @GET
    @Path(PERSON_PATH)
    public String getPerson(@QueryParam("name") String name) {
        Optional<ODocument> result = peopleDao.getPeople().stream().filter(person -> person.field("name").equals(name))
                .findFirst();

        return result.isPresent() ? result.get().field("name") : null;
    }

    @POST
    @Path(PERSON_PATH)
    public void addPerson(@QueryParam("name") String name) {
        peopleDao.addPerson(name);
    }

}
