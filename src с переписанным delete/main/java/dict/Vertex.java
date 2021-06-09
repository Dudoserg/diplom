package dict;

import lombok.Getter;
import lombok.Setter;
import utils.Pair;

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

    public Vertex(Word word) {
        this.word = word;
    }




//    public static Vertex getVertex(DictBase dictBase, String word) {
//        Word w = Word.getWord(word);
//        Vertex v = dictBase.vertex_cash.get(w);
//        if (v == null) {
//            v = new Vertex(w);
//            dictBase.vertex_cash.put(w, v);
//        }
//        return v;
//    }
//
//    public static Vertex getVertex(DictBase dictBase, String word, PartOfSpeech partOfSpeech) {
//        Word w = Word.getWord(word);
//        w.setPartOfSpeech(partOfSpeech);
//        Vertex v = dictBase.vertex_cash.get(w);
//        if (v == null) {
//            v = new Vertex(w);
//            dictBase.vertex_cash.put(w, v);
//        }
//        return v;
//    }


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


    List<Pair<Cluster, Integer>> shortest;
    public List<Pair<Cluster, Integer>> getShortest(){
        if(this.shortest == null){
            this.shortest = new ArrayList<>();
            for (Pair<Cluster, Integer> clusterIntegerPair : this.clusterList) {
                Cluster key = clusterIntegerPair.getFirst();
                Integer value = clusterIntegerPair.getSecond();

                Pair<Cluster, Integer> resultContain = shortest.stream()
                        .filter(r -> r.getFirst().getVertex().equals(key.getVertex()))
                        .findFirst()
                        .orElse(null);

                if(resultContain == null){
                    shortest.add(clusterIntegerPair);
                }
            }
        }
        shortest.sort((o1, o2) -> Integer.compare(o1.getSecond(), o2.getSecond()));
        return shortest;
    }

}
