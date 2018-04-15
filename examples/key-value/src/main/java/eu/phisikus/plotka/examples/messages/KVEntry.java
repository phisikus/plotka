package eu.phisikus.plotka.examples.messages;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Objects;

public class KVEntry implements Serializable, scala.Serializable {
    private static final long serialVersionUID = 42L;

    private String key;
    private String value;
    private Long timestamp;

    public KVEntry() {
    }

    public KVEntry(String key, String value, Long timestamp) {
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KVEntry that = (KVEntry) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, timestamp);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("key", key)
                .append("value", value)
                .append("timestamp", timestamp)
                .toString();
    }
}
