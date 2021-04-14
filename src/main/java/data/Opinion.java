package data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Opinion {
    private String target;
    private String category;
    private String polarity;
    private int from;
    private int to;
}
