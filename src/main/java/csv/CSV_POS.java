package csv;

public enum  CSV_POS {
    Noun(1),
    Adjective(2),
    Participle(3),
    Infinitive(4),
    Verb(5),
    Adverb(6),
    DParticiple(7),
    Unknown(8);


    private final int value;

    CSV_POS(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
}
