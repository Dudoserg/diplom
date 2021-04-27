package mystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class MyStem {
    private static final String MYSTEM_exe = "mystem" + File.separator  + "exe" + File.separator + "mystem.exe";
    private static final String MYSTEM_RESULT_json = "mystem" + File.separator + "mystemResult.json";
    public static final String FULL_TEXT = "mystem" + File.separator + "full_text.txt";
    public static final String TEXT_WITHOUT_STOPWORDS_txt = "mystem" + File.separator + "text_withoutStopWord.txt";

    private String id;

    public MyStem(List<String> words, String id) {
        this.id = id;
        this.words = words;
        this.text = words.stream().collect(Collectors.joining(" ")).trim();
    }

    public MyStem(String text, String id) throws IOException {
        this.id = id;
        text = text.replace("\r\n", " ");
        text = text.replace("\n", " ");
        text = text.replace(".", ". ");
        text = text.replace(",", ", ");
        text = text.replace("!", "! ");
        text = text.replace("?", "? ");
        this.text = text.trim().replaceAll(" +", " ").toLowerCase();
        this.words = Arrays.stream(this.text.split(" "))
                .filter(s -> !s.equals(" "))
                .collect(Collectors.toList());
    }


    private String text;
    private List<String> words;
    private StopWords stopWords;

    MyStemResult myStemResult;

    public MyStem removeStopWord() {
        if (stopWords == null) {
            stopWords = StopWords.getInstance();
        }
        MyStem result = new MyStem(words, id);
        result.words = result.words.stream()
                .filter(s -> {
                    String tmp = s.trim().replaceAll("[ .?!,]", "");
                    if (stopWords.contains(tmp))
                        return false;
                    return true;
                })
                .collect(Collectors.toList());
        result.text = result.words.stream().collect(Collectors.joining(" ")).trim();
        return result;
    }

    public void saveToFile(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(addId(path)));
        writer.write(text);
        writer.close();
    }


    public void lemmatization() {
        System.out.print("myStemPath execution...");
        try {
            String command = MYSTEM_exe + " " + addId(TEXT_WITHOUT_STOPWORDS_txt) + " " + addId(MYSTEM_RESULT_json) + " " +
                    "--format json -c -l -s -i ";
            Process p = Runtime.getRuntime()
                    .exec(command);
            p.waitFor();
            String json = Helper.readFile(addId(MYSTEM_RESULT_json));

            Helper.saveToFile(json, addId("-" + File.separator + "mystemResult.json"));
            ObjectMapper objectMapper = new ObjectMapper();

            // читаем результаты работы лемманизатора MyStem
            MyStemItem[] myStemItems = objectMapper.readValue(json, MyStemItem[].class);
            ArrayList<MyStemItem> objects = new ArrayList<>(Arrays.asList(myStemItems));
            objects.forEach(MyStemItem::calcPartOfSpeech);
            MyStemItem.setPrint();
            setMyStemResult(new MyStemResult(objects));

            // Устанавливаем флаг - является ли этот элемент стоп словом
            for (MyStemItem myStemItem : this.myStemResult.getItemList()) {

                if (myStemItem.getAnalysisList() == null || myStemItem.getAnalysisList().size() == 0)
                    continue;

                for (MyStemAnalysis myStemAnalysis : myStemItem.getAnalysisList()) {
                    if (stopWords.contains(myStemAnalysis.getLex())) {
                        myStemAnalysis.setStopWord(true);
                    }
                }

            }
        } catch (Exception ex) {
            System.out.println("exception is:" + ex);
        }
        System.out.println("\t\t\tdone");
    }

    public void removeStopWordsFromLemmatization() {
        for (int i = this.myStemResult.getItemList().size() - 1; i >= 0; i--) {
            MyStemItem myStemItem = this.myStemResult.getItemList().get(i);
            try {
                if (myStemItem.isOneOfAnalysisStopWord())
                    this.myStemResult.getItemList().remove(myStemItem);
            } catch (UnsupportedOperationException e) {
                throw e;
            }
        }
    }


    public String addId(String str){
        String[] split = str.split("\\" + File.separator);
        String result = "";
        for(int i = 0 ; i < split.length - 1; i++){
            result = result + split[i] + File.separator;
        }
        result += id + "_" + split[split.length - 1];
        return result;
    }

}
