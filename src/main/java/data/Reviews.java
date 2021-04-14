package data;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@JacksonXmlRootElement(localName = "Reviews")
public class Reviews {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Review")
    private List<Review> review;


    public static Reviews readFromFile(String path) throws IOException {
        File file = new File(path);
        String xml = inputStreamToString(new FileInputStream(file));

        XmlMapper xmlMapper = new XmlMapper();

        return xmlMapper.readValue(xml, Reviews.class);
    }

    private static String inputStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }


    public List<List<String>> getSentence(){
        return review.stream()
                .map(Review::getSentence)
                .collect(Collectors.toList());
    }

    public List<String> getTexts(){
        return review.stream()
                .map(Review::getText)
                .collect(Collectors.toList());
    }

}
