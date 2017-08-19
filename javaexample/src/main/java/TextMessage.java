import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Objects;

public class TextMessage implements Serializable {
    private static final long serialVersionUID = 42L;
    private String text;

    public TextMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextMessage that = (TextMessage) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("text", text)
                .toString();
    }
}
