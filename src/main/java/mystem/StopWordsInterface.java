package mystem;

import java.util.List;

public interface StopWordsInterface {
    List<String> getStopWords();

    void ignore(String word);
}
