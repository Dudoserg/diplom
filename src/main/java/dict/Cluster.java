package dict;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Cluster {
    private Vertex center;

    private double weight;

    private int countVertex = 0;

    private List<Vertex> vertexList;

    public Cluster(Vertex center) {
        this.center = center;
    }
}
