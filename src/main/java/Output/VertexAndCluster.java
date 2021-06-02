package Output;

import dict.Cluster;
import dict.Vertex;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VertexAndCluster {
    Vertex vertex;
    Cluster cluster;
    int distance;

    public VertexAndCluster(Vertex vertex, Cluster cluster, int distance) {
        this.vertex = vertex;
        this.cluster = cluster;
        this.distance = distance;
    }
}