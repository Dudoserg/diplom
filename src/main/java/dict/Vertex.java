package dict;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class Vertex {
    public static Map<Word, Vertex> vertex_cash;
    static {
        vertex_cash = new HashMap<>();
    }

    static final int p = 31;
    static int HSIZE = 10;

    private Word word;
    private double weight = 0.0;

    private Vertex(Word word) {
        this.word = word;
    }

    public static Vertex getVertex(String word){
        Word w = Word.getWord(word);
        Vertex v =  vertex_cash.get(w);
        if (v == null) {
            v = new Vertex(w);
            vertex_cash.put(w, v);
        }
        return v;
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
