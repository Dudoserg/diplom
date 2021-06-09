package Main.Analyzer.SentiResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordAndValue {
    private String cluster;
    private String sentimentMark;

    public WordAndValue(String cluster, String sentimentMark) {
        this.cluster = cluster;
        this.sentimentMark = sentimentMark;
    }
}
