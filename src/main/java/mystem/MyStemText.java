package mystem;

import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class MyStemText {
    public MyStemText(List<String> words) {
        this.words = words;
        this.text = words.stream().collect(Collectors.joining(" ")).trim();
    }
    public MyStemText(String text) throws IOException {
        text = text.replace("\r\n", " ");
        text = text.replace("\n", " ");
        text = text.replace(".", ". ");
        text = text.replace(",", ", ");
        text = text.replace("!", "! ");
        text = text.replace("?", "? ");
        this.text = text.trim().replaceAll(" +", " ");
        this.words = Arrays.stream(this.text.split(" "))
                .filter(s -> !s.equals(" "))
                .collect(Collectors.toList());
    }


    private String text;
    private List<String> words;
    private static List<String> stopWords = null;



    public MyStemText removeStopWord(){
        if(stopWords == null){
            stopWords = Helper.readFileLineByLine("mystem" + File.separator + "stop_words.txt");
        }
        MyStemText result = new MyStemText(words);
        result.words = result.words.stream()
                .filter(s -> {
                    String tmp = s.trim().replaceAll("[ .?!,]", "");
                    if(stopWords.contains(tmp))
                        return false;
                    return true;
                })
                .collect(Collectors.toList());
        result.text = result.words.stream().collect(Collectors.joining(" ")).trim();
        return result;
    }

    public void saveToFile(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(text);
        writer.close();

    }
}
