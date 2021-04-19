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
    private static final String MYSTEM_exe = "mystem" + File.separator + "mystem.exe";
    private static final String MYSTEM_RESULT_json = "mystem" + File.separator + "mystemResult.json";

    public static final String FULL_TEXT = "-" + File.separator + "full_text.txt";
    public static final String TEXT_WITHOUT_STOPWORDS_txt = "mystem" + File.separator + "text_withoutStopWord.txt";

    public MyStem(List<String> words) {
        this.words = words;
        this.text = words.stream().collect(Collectors.joining(" ")).trim();
    }

    public MyStem(String text) throws IOException {
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
    private StopWords stopWords;

    MyStemResult myStemResult;

    public MyStem removeStopWord() {
        if (stopWords == null) {
            stopWords = StopWords.getInstance();
        }
        MyStem result = new MyStem(words);
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
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(text);
        writer.close();
    }


    public void lemmatization() {
        System.out.print("myStemPath execution...");
        try {
            Process p = Runtime.getRuntime()
                    .exec(MYSTEM_exe + " " + TEXT_WITHOUT_STOPWORDS_txt + " " + MYSTEM_RESULT_json + " " + "--format json -c -l -s -i");
            p.waitFor();
            String json = Helper.readFile(MYSTEM_RESULT_json);

            Helper.saveToFile(json, "-" + File.separator + "mystemResult.json");
            ObjectMapper objectMapper = new ObjectMapper();

            // читаем результаты работы лемманизатора MyStem
            MyStemItem[] myStemItems = objectMapper.readValue(json, MyStemItem[].class);
            ArrayList<MyStemItem> objects = new ArrayList<>(Arrays.asList(myStemItems));
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
}
