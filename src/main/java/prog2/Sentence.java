package prog2;

import dict.Cluster;
import lombok.Getter;
import lombok.Setter;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Sentence {

    private List<WordOfSentence> wordOfSentenceList = new ArrayList<>();
    private String processedText = "";
    List <Pair<Cluster, Double>> result;


    public void addElemToProcessedText(String elem) {
        processedText += elem + " ";
    }

}
