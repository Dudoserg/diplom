package dict;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RelationType {
    DEF, SYN, ASS, UNKNOWN;

    @JsonValue
    public int toValue() {
        return ordinal();
    }
}
