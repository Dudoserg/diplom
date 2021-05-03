package dict;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
public class Vertex implements Serializable {
    static final int p = 31;
    static int HSIZE = 10;

    private Word word;
    private double weight = 0.0;
    private double weightOutgoingVertex = 0.0;
    private boolean flag_train = false;

    // список кластеров в которые входит вершины
    private List<Pair<Cluster, Integer>> clusterList = new ArrayList<>();

    private Vertex(Word word) {
        this.word = word;
    }

    public static Vertex getVertex(DictBase dictBase, String word) {
        Word w = Word.getWord(word);
        Vertex v = dictBase.vertex_cash.get(w);
        if (v == null) {
            v = new Vertex(w);
            dictBase.vertex_cash.put(w, v);
        }
        return v;
    }

    public static Vertex getVertex(DictBase dictBase, String word, PartOfSpeech partOfSpeech) {
        Word w = Word.getWord(word);
        w.setPartOfSpeech(partOfSpeech);
        Vertex v = dictBase.vertex_cash.get(w);
        if (v == null) {
            v = new Vertex(w);
            dictBase.vertex_cash.put(w, v);
        }
        return v;
    }


    public boolean isNoun() {
        return this.word.isNoun();
    }

    public void addCluster(Cluster cluster, int radius) {
        clusterList.add(new Pair<>(cluster, radius));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex1 = (Vertex) o;
        boolean isEqual = Objects.equals(word, vertex1.word);
        return isEqual;
    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }


}
