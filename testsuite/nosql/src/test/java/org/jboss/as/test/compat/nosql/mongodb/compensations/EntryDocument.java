package org.jboss.as.test.compat.nosql.mongodb.compensations;

import org.bson.Document;
import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

/**
 * Compensation scoped object to share data between service and handlers.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@CompensationScoped
public class EntryDocument implements Serializable {

    private Document document;

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

}
