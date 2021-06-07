package Main.Analyzer;

import Main.Main;
import dict.Cluster;
import lombok.Getter;
import lombok.Setter;
import prog2.Sentence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class AnalyzerRezult {
    private List<Sentence> sentenceList = new ArrayList<>();

    public void addSentence(Sentence sentence){
        this.sentenceList.add(sentence);
    }
}
