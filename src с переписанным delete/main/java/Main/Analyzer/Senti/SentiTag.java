package Main.Analyzer.Senti;

public enum SentiTag {
    NGTV("NGTV"),
    NEUT("NEUT"),
    PSTV("PSTV");

    SentiTag(String str) {
        this.str = str;
    }

    private String str;

    public String getStr() {
        return str;
    }

    public static SentiTag getPart(String str) {
        for (SentiTag value : SentiTag.values()) {
            if (str.equals(value.str))
                return value;
        }
        return null;
    }
}
