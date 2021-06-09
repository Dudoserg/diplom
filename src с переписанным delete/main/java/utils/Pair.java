package utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pair <F,S>{
    private F first;
    private S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }


}
