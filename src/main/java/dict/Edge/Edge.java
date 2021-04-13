package dict.Edge;

import dict.RelationType;
import dict.Vertex;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Edge {
    private RelationType relationType;
//    private double def_weight = 0.0;
//    private double syn_weight = 0.0;
//    private double ass_weight = 0.0;
    private double weight = 0.0;

    private Vertex from;
    private Vertex to;

    public Edge(Vertex from, Vertex to, double weight, RelationType relationType) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.relationType = relationType;
    }

}
