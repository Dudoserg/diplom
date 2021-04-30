package dict;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cluster {
    private Vertex center;

    public Cluster(Vertex center) {
        this.center = center;
    }
}
