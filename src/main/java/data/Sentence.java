package data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@JacksonXmlRootElement(localName="sentence")
public class Sentence {
    @JacksonXmlProperty(isAttribute=true)
    private String id;


    @JacksonXmlProperty(isAttribute=true)
    private String OutOfScope;

    @JacksonXmlElementWrapper(useWrapping=false)
    private String text;

    @JacksonXmlElementWrapper(useWrapping=true)
    @JacksonXmlProperty(localName = "Opinions")
    private List<Opinion> opinions;




}
