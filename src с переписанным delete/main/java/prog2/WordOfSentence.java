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

    public WordOfSentence(String word) {
        this.word = word;
        this.vertex = null;
    }

    /**
     * @param cluster  Кластер  к которому относится данное слово в графе
     * @param distance дистанция до центра кластера
     */
    public void addCluster(Cluster cluster, int distance) {
        this.func.put(cluster, distance);
    }
}
