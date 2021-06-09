package data;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JacksonXmlRootElement(localName = "Reviews")
public class Reviews {

    public static final String RU_TRAIN_PATH = System.getProperty("user.dir") + File.separator +
            "data" + File.separator + "semeval" + File.separator +
            "restaurant" + File.separator + "train" + File.separator + "se16_ru_rest_train.xml";
    public static final String RU_TRAIN_PATH_2= System.getProperty("user.dir") + File.separator +
            "data" + File.separator + "semeval" + File.separator +
            "restaurant" + File.separator + "train" + File.separator + "se16_ru_rest_train_task2.xml";

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Review")
    private List<Review> review;


    public static Reviews readFromFile(String path) throws IOException {
        System.out.print("read train data from file...\t\t");
        Long startTime = System.currentTimeMillis();
        File file = new File(path);
        String xml = inputStreamToString(new FileInputStream(file));

        XmlMapper xmlMapper = new XmlMapper();

        System.out.println("done for " + (System.currentTimeMillis() - startTime) + " ms.");
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
