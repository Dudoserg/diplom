package mystem;

import constant.CONST;
import utils.Helper;

import java.io.File;
import java.util.List;

public class StopWords implements StopWordsInterface{

    private static StopWords instance;
    public List<String> stopWords = null;


    private StopWords(List<String> l) {
        this.stopWords = l;
    }

    public static StopWords getInstance() {
        if (instance == null) {
            instance = new StopWords(
                    Helper.readFileLineByLine(
                            CONST.SETTING_STOPWORDS_PATH
                    )
            );
        }
        return instance;
    }

    public boolean contains(String tmp) {
        return this.stopWords.contains(tmp);
    }

    @Override
    public List<String> getStopWords() {
        return StopWords.getInstance().stopWords;
    }

    public void ignore(String word) {
        stopWords.remove(word);
    }
}
