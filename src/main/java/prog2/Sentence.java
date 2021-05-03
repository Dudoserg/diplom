package prog2;

import dict.Cluster;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class Sentence {
    String word;
    HashMap<Cluster, Double> func = new HashMap<>();

    public Sentence(String word) {
        this.word = word;
    }
}
