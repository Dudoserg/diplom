package mystem;

import utils.Helper;

import java.io.File;
import java.util.List;

public class StopWords {

    private static StopWords instance;
    public List<String> stopWords = null;


    private StopWords(List<String> l) {
        // Этот код эмулирует медленную инициализацию.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        this.stopWords = l;
    }

    public static StopWords getInstance() {
        if (instance == null) {
            instance = new StopWords(Helper.readFileLineByLine("mystem" + File.separator + "stop_words2.txt"));
        }
        return instance;
    }

    public boolean contains(String tmp) {
        return this.stopWords.contains(tmp);
    }
}
