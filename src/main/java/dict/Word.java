package dict;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Word {
    static final int p = 31;
    static int HSIZE = 10;
    public static Map<String, Word> words_cash;

    static {
        words_cash = new HashMap<>();
    }

    /**
     * Constructor
     *
     * @param str
     * @return
     */
    public static Word getStr(String str) {
        Word w = words_cash.get(str);

        if (w == null) {
            w = new Word(str);
            words_cash.put(str, w);
        } else
            System.out.println(""); //TODO
        return w;
    }

    public Word(String str, WordType wt) {
        this.str = str;
        this.wordType = wt;
    }
    public Word(String str) {
        this.str = str;
    }

    private String str;
    private WordType wordType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word1 = (Word) o;
        return str.equals(word1.str);
    }

    @Override
    public int hashCode() {
        char[] arrayChars = str.toCharArray();
        long hash = 0;
        long p_pow = 1;
        for (int i = 0; i < arrayChars.length; i++) {
            // желательно отнимать 'a' от кода буквы
            // единицу прибавляем, чтобы у строки вида 'aaaaa' хэш был ненулевой
            hash += (arrayChars[i] - 'a' + 1) * p_pow;
            p_pow *= p;
        }
        //int x = (int) (hash % HSIZE); //return Objects.hash(word);
        return (int) hash;
    }
}
