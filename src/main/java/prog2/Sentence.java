package prog2;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Sentence {
    private List<WordOfSentence> wordOfSentenceList = new ArrayList<>();
    private String processedText = "";


    public void addElemToProcessedText(String elem) {
        processedText += elem + " ";
    }

}
