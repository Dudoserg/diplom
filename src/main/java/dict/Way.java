package dict;

import dict.Edge.Edge;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Way {
    private List<Edge> way;
    private double weight;
    private double length;

    public Way(List<Edge> way, double weight, double length) {
        this.way = way;
        this.weight = weight;
        this.length = length;
    }

    public boolean isEmpty(){
        return way == null || way.size() == 0;
    }

    public String print() {
        StringBuilder str = new StringBuilder();

        Edge edge = way.get(0);
        str.append(edge.getFrom().getWord().getStr() + " ");
        for (int i = 1; i < length; i++) {
            edge = way.get(i);
            str.append(edge.getFrom().getWord().getStr() + " ");
        }
        str.append(edge.getTo().getWord().getStr() + " ");
        str.append("\t");
        str.append("W=" + weight);
        str.append("\t");

        str.append("L=" + length);
        return str.toString();
    }
}
