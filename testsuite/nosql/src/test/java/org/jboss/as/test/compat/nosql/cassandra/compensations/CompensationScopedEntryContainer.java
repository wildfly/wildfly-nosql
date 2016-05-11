package org.jboss.as.test.compat.nosql.cassandra.compensations;

import org.jboss.narayana.compensations.api.CompensationScoped;

import java.io.Serializable;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@CompensationScoped
public class CompensationScopedEntryContainer implements Serializable {

    private Entry entry;

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }
}
