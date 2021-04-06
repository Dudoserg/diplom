package dict;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class EdgeMap {
    private Map<Vertex, Edge> edgeMap = new HashMap<>();


    public void addEdge(Vertex vertex, Edge edge) {
        this.edgeMap.put(vertex, edge);
    }

    public int size() {
        return this.edgeMap.size();
    }

    public boolean isEmpty() {
        return this.edgeMap.size() == 0;
    }
}
