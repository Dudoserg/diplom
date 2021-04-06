package dict;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Vertex {
    static final int p = 31;
    static int HSIZE = 10;

    private Word word;
    private double weight = 0.0;

    public Vertex(String word) {
        this.word = Word.getStr(word);
    }
    public Vertex(Word word) {
        this.word = word;
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
