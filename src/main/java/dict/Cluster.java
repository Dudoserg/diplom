package dict;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Cluster implements Serializable {
    private Vertex vertex;
    private double weight;
    private List<Vertex> vertexList = new ArrayList<>();


    public Cluster(Vertex vertex) {
        this.vertex = vertex;
    }

}
