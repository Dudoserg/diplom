package data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter

@JacksonXmlRootElement(localName="Review")
public class Review {
    @JacksonXmlProperty(isAttribute=true)
    private String rid;

    @JacksonXmlElementWrapper(useWrapping=true)
    private List<Sentence> sentences;


    public List<String> getSentence(){
        return sentences.stream()
                .map(Sentence::getText)
                .collect(Collectors.toList());
    }
    public String getText(){
        return sentences.stream()
                .map(Sentence::getText)
                .collect(Collectors.joining(" "));
    }
}
