package utils;


import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Unigram {
    public Unigram(String first) {
        this.first = first;
    }

    private int frequency = 0;
    private String first;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unigram unigram = (Unigram) o;
        return first.equals(unigram.first);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first);
    }
}
