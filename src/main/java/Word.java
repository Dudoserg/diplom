import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Word {
    public static Map<String, Word> words_cash;

    static {
        words_cash = new HashMap<>();
    }

    public static Word getWord(String str) {
        Word w = words_cash.get(str);

        if (w == null)
            w = new Word(str);

        return w;
    }

    public Word(String word) {
        this.word = word;
    }

    private String word;

}
