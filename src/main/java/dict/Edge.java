package dict;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Edge {
    private RelationType relationType;
    private double def_weight = 0.0;
    private double syn_weight = 0.0;
    private double ass_weight = 0.0;

    public Edge(RelationType relationType) {
        this.relationType = relationType;
    }
    public Edge(double def, double syn, double ass) {
        this.relationType = RelationType.DEF;
        this.def_weight = def;
        this.syn_weight = syn;
        this.ass_weight = ass;
    }
}
