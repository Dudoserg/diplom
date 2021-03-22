import java.util.Objects;

public class Word {
    static final int p = 31;
    static  int HSIZE = 10;

    private String word;

    public Word(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        boolean isEqual = Objects.equals(word, word1.word);
        return isEqual;
    }

    @Override
    public int hashCode() {
        char[] arrayChars = word.toCharArray();
        long hash = 0;
        long p_pow = 1;
        for (int i = 0; i < arrayChars.length ; i++) {
            // желательно отнимать 'a' от кода буквы
            // единицу прибавляем, чтобы у строки вида 'aaaaa' хэш был ненулевой
            hash += (arrayChars[i] - 'a' + 1) * p_pow;
            p_pow *= p;
        }
        //int x = (int) (hash % HSIZE); //return Objects.hash(word);
        return (int) hash;
    }
}
