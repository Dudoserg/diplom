package dict;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClusterHelper {
    private Vertex vertex;

    private int place_weight = -1;

    private int place_weightOutgoing = -1;

    private double clusterWeight = 0.0;

    public ClusterHelper(Vertex vertex) {
        this.vertex = vertex;
    }
}
