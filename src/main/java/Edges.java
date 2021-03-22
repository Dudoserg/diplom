import java.util.HashMap;
import java.util.Map;

public class Edges {
    private Map<Word, RelationType> edgeMap = new HashMap<>();

    public Map<Word, RelationType> getEdgeMap() {
        return edgeMap;
    }

    public void setEdgeMap(Map<Word, RelationType> edgeMap) {
        this.edgeMap = edgeMap;
    }


    public void addWord(Word word, RelationType relationType){
        this.edgeMap.put(word, relationType);
    }
}
