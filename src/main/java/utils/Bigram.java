package utils;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Bigram {
    public Bigram(String first, String second) {
        this.first = first;
        this.second = second;
    }

    private String first;
    private String second;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bigram bigram = (Bigram) o;
        return first.equals(bigram.first) && second.equals(bigram.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
