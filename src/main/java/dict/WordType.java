package dict;

public enum WordType {
    Noun(1),
    Adjective(2),
    Participle(3),
    Infinitive(4),
    Verb(5),
    Adverb(6),
    DParticiple(7),
    Unknown(8);


    private final int value;

    WordType(final int newValue) {
        value = newValue;
    }

    public static WordType create(int n) throws DictException {
        for (WordType wordType : WordType.values()) {
            if (wordType.getValue() == n)
                return wordType;
        }
        throw new DictException("WortType #" + n + " is unknown!");
    }

    public int getValue() {
        return value;
    }
}
