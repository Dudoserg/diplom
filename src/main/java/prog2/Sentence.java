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

    public void setResultMap(Map<Cluster, Double> result) {
        this.result = new ArrayList<>();
        for (Map.Entry<Cluster, Double> clusterDoubleEntry : result.entrySet()) {
            Double value = clusterDoubleEntry.getValue();
            Cluster key = clusterDoubleEntry.getKey();
            this.result.add(new Pair<>(key, value));
        }
        this.result.sort((o1, o2) -> -Double.compare(o1.getSecond(), o2.getSecond()));
    }
}
