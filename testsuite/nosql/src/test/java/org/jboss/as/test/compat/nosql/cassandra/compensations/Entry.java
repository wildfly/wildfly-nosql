package org.jboss.as.test.compat.nosql.cassandra.compensations;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class Entry {

    private final String value;

    private final boolean confirmed;

    public Entry(String value, boolean confirmed) {
        this.value = value;
        this.confirmed = confirmed;
    }

    public String getValue() {
        return value;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Entry entry = (Entry) o;

        if (confirmed != entry.confirmed) {
            return false;
        }

        return !(value != null ? !value.equals(entry.value) : entry.value != null);

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (confirmed ? 1 : 0);

        return result;
    }

    @Override
    public String toString() {
        return "Entry{" + "value='" + value + '\'' + ", confirmed=" + confirmed + '}';
    }
}
