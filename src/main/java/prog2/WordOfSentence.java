package prog2;

import dict.Cluster;
import dict.Vertex;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class WordOfSentence {
    String word;
    Vertex vertex;
    HashMap<Cluster, Integer> func = new HashMap<>();

    public WordOfSentence(String word, Vertex vertex) {
        this.word = word;
        this.vertex = vertex;
    }
}
