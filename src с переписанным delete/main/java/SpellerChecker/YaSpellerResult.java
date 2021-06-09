package SpellerChecker;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class YaSpellerResult {
    private int code;
    private String word;
    private int count;
    private List<String> suggest;
    private List<Position> position;
}
