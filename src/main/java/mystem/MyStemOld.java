package mystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import prog2.Sentence;
import prog2.WordOfSentence;
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
public class MyStemOld {
    private static final String MYSTEM_exe = "mystem" + File.separator + "exe" + File.separator + "mystem.exe";
    private static final String MYSTEM_RESULT_json = "mystem" + File.separator + "mystemResult.json";
    public static final String FULL_TEXT = "mystem" + File.separator + "full_text.txt";
    public static final String TEXT_WITHOUT_STOPWORDS_txt = "mystem" + File.separator + "text_withoutStopWord.txt";

    private String id;


    private String baseString;
    private String text;
    private List<String> words;
    private StopWords stopWords;

    MyStemResult myStemResult;


    public MyStemOld(List<String> words, String id) {
        this.id = id;
        this.words = words;
        this.baseString = this.text = words.stream().collect(Collectors.joining(" ")).trim();

        if (stopWords == null) {
            stopWords = StopWords.getInstance();
        }
    }

    public MyStemOld(String text, String id) throws IOException {
        this.id = id;
        text = text.replace("\r\n", " ");
        text = text.replace("\n", " ");
        text = text.replace(".", ". ");
        text = text.replace(",", ", ");
        text = text.replace("!", "! ");
        text = text.replace("?", "? ");
        this.baseString = this.text = text.trim().replaceAll(" +", " ").toLowerCase();
        this.words = Arrays.stream(this.text.split(" "))
                .filter(s -> !s.equals(" "))
                .collect(Collectors.toList());

        if (stopWords == null) {
            stopWords = StopWords.getInstance();
        }
    }

    public MyStemOld removeStopWord() {
        if (stopWords == null) {
            stopWords = StopWords.getInstance();
        }
        MyStemOld result = new MyStemOld(words, id);
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


    public void lemmatization() throws IOException, InterruptedException {
        System.out.print("myStemPath execution...\t\t");
        Long startTime = System.currentTimeMillis();
        saveToFile(TEXT_WITHOUT_STOPWORDS_txt);
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
                    try {
                        if (stopWords.contains(myStemAnalysis.getLex())) {
                            myStemAnalysis.setStopWord(true);
                        }
                    } catch (NullPointerException e) {
                        throw e;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("exception is:" + ex);
            throw ex;
        }
        System.out.println("done for " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    public void removeTmpFiles() {
        this.removeFile(TEXT_WITHOUT_STOPWORDS_txt);
        this.removeFile(MYSTEM_RESULT_json);
    }

    private boolean removeFile(String text_without_stopwords_txt) {
        File f = new File(addId(text_without_stopwords_txt));
        return f.delete();
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


    public String addId(String str) {
        String[] split = str.split("\\" + File.separator);
        String result = "";
        for (int i = 0; i < split.length - 1; i++) {
            result = result + split[i] + File.separator;
        }
        result += id + "_" + split[split.length - 1];
        return result;
    }


    public List<List<String>> getSentencesList() {
        List<List<String>> sentencesList = new ArrayList<>();
        List<String> sentence = new ArrayList<>();
        for (MyStemItem myStemItem : this.getMyStemResult().getItemList()) {
            List<MyStemAnalysis> analysisList = myStemItem.getAnalysisList();
            if (".".equals(myStemItem.getText().trim())) {
                sentencesList.add(sentence);
                sentence = new ArrayList<>();
                System.out.println(".");
                continue;
            }
            if (analysisList != null && analysisList.size() >= 1) {
                MyStemAnalysis myStemAnalysis = analysisList.get(0);
                System.out.println(myStemAnalysis.getLex());
                sentence.add(myStemAnalysis.getLex());
            }
        }
        return sentencesList;
    }


    public List<Sentence> getSentencesList2() {
        List<String> collect = Arrays.stream(this.baseString.split("\\."))
                .map(elem -> elem + ".")
                .collect(Collectors.toList());

        int it = 0;
        List<Sentence> sentencesList = new ArrayList<>();
        Sentence sentence = new Sentence();
        for (MyStemItem myStemItem : this.getMyStemResult().getItemList()) {
            List<MyStemAnalysis> analysisList = myStemItem.getAnalysisList();
            sentence.addElemToProcessedText(myStemItem.getText().trim());
            if (".".equals(myStemItem.getText().trim())) {
                sentencesList.add(sentence);
                sentence.setProcessedText(collect.get(it++));
                sentence = new Sentence();
                continue;
            }
            if (analysisList != null && analysisList.size() >= 1) {
                MyStemAnalysis myStemAnalysis = analysisList.get(0);
//                System.out.println(myStemAnalysis.getLex());
                sentence.getWordOfSentenceList().add(new WordOfSentence(myStemAnalysis.getLex()));
            }
        }
        return sentencesList;
    }

}