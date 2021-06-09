package Main.Analyzer.SentiResult;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WordAndClusters {
    private String word;
    private List<ClusterAndDistance> clusterAndDistanceList = new ArrayList<>();

    public WordAndClusters(String word) {
        this.word = word;
    }
}
