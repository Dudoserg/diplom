package Main.Analyzer.SentiResult;

import lombok.Getter;
import lombok.Setter;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SentiResult {
    private List<WordAndValue> result = new ArrayList<>();
    private List<SentiResultSentence> sentiResultSentenceList = new ArrayList<>();
}
