package dict;



public enum PartOfSpeech {
    NOUN("S"),
    ADJECTIVE("A"),
    VERB("V"),
    SPRO("SPRO"),
    APRO("APRO"),
    ANUM("ANUM"),
    ADV("ADV"),
    ADVPRO("ADVPRO"),
    PART("PART"),
    PR("PR"),       // у
    CONJ("CONJ"),       // или
    INTJ("INTJ"),       // или
    COM("COM"),       // или
    NUM("NUM"); // числительное
    PartOfSpeech(String str) {
        this.str = str;
    }

    private String str;

    public String getStr() {
        return str;
    }

    public static PartOfSpeech getPart(String str){
        for (PartOfSpeech value : PartOfSpeech.values()) {
            if (str.equals(value.str))
                return value;
        }
        return null;
    }
}
