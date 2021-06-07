package Main.Analyzer.SentiResult;

import lombok.Getter;
import lombok.Setter;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SentiResultSentence {
    private String originalStr;
    private String lemmatizationStr;
    private double sentenceSentiMark;
    private List<WordAndClusters> wordAndClustersList = new ArrayList<>();
    private List<Pair<String, Double>> clustersAndWeightList = new ArrayList<>();

}
