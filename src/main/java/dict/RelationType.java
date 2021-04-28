package dict;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RelationType {
    DEF("DEF"),
    SYN("SYN"),
    ASS("ASS"),
    UNKNOWN("UNKNOWN");

    private String str;

    public String getStr() {
        return str;
    }

    RelationType(String str) {
        this.str = str;
    }

    public  RelationType getValueByStr(String str){
        for (RelationType value : values()) {
            if (str.equals(value.getStr()))
                return value;
        }
        return null;
    }

    @JsonValue
    public int toValue() {
        return ordinal();
    }
}
