package Main.Analyzer;

import Main.Analyzer.Senti.SentiLoader;
import Main.Analyzer.Senti.SentiValue;
import Main.Analyzer.Senti.SentimentDictionary;
import prog2.Sentence;
import prog2.WordOfSentence;

public class SentiAnalyze {
    private final SentimentDictionary dictionary;

    public SentiAnalyze() {
        SentiLoader sentiLoader = new SentiLoader();
         dictionary = sentiLoader.load();
    }

    public Double analyze(Sentence sentence){
        double plusSenti = 0.0;
        double minusSenti = 0.0;

        int count = 0;
        String prev = null;
        for (WordOfSentence wordOfSentence : sentence.getWordOfSentenceList()) {
            String str = wordOfSentence.getWord();
            double sentiValue = getSentiByWord(str);
            if("не".equals(prev)){
                sentiValue *= -1;
            }
            if(sentiValue >= 0)
                plusSenti += sentiValue;
            else
                minusSenti += sentiValue;
            prev = str;
        }
        return plusSenti + minusSenti;
    }

    private double getSentiByWord(String word){
        SentiValue sentiValue = dictionary.getByStr(word);
        if(sentiValue == null){
            System.out.print("");
            return 0.0;
        }
        return sentiValue.getValue();
    }
}
