package Main.Analyzer.SentiResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/// центр класстера, расстояние до центра
public class ClusterAndDistance {
    private String Cluster;
    private Integer distance;

    public ClusterAndDistance(String cluster, Integer distance) {
        Cluster = cluster;
        this.distance = distance;
    }
}
