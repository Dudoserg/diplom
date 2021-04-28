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
    public static Word getWord(String str) {
        str = str.toLowerCase();
        Word w = words_cash.get(str);

        if (w == null) {
            w = new Word(str);
            words_cash.put(str, w);
        } else
            System.out.print(""); //TODO
        return w;
    }

    private Word(String str, WordType wt) {
        this.str = str.toLowerCase();
        this.wordType = wt;
    }

    private Word(String str) {
        this.str = str.toLowerCase();
    }

    private String str;
    private WordType wordType;
    private PartOfSpeech partOfSpeech;

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


    public boolean isNoun(){
        return PartOfSpeech.NOUN.equals(this.partOfSpeech);
    }
}
