


public class Edge {

    private RelationType relationType;
    private double weight = 0.0;

    public Edge(RelationType relationType) {
        this.relationType = relationType;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
