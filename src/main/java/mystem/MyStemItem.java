package mystem;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dict.PartOfSpeech;
import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Getter
@Setter
public class MyStemItem {
    @JsonProperty("analysis")
    private List<MyStemAnalysis> analysisList;
    private String text;
    private PartOfSpeech partOfSpeech;

    /**
     * Является ли этот айтем разделителем
     *
     * @return
     */
    public boolean isDelimeter() {
        return analysisList == null || analysisList.size() == 0;
    }

    public boolean isPoint() {
        return text.trim().equals(".");
    }

    public String getBaseForm() {
        return analysisList.get(0).getLex();
    }

    public boolean isOneOfAnalysisStopWord() {
        if (analysisList != null && analysisList.size() > 0)
            for (MyStemAnalysis myStemAnalysis : analysisList) {
                if (myStemAnalysis.isStopWord())
                    return true;
            }
        return false;
    }

    static TreeSet<String> set = new TreeSet<>();

    static Map<String, TreeSet<String>> allWordsByTypes = new HashMap<>();

    public void calcPartOfSpeech(){
        // TODO
        if(analysisList != null && analysisList.size() != 0){
            String partOfSpeechStr = analysisList.get(0).getGr().split("[=,]")[0];
            for (MyStemAnalysis myStemAnalysis : analysisList) {
                set.add(myStemAnalysis.getGr());
            }
            partOfSpeech = PartOfSpeech.getPart(partOfSpeechStr);
            if(partOfSpeech == null){
                System.out.println("unknown part of speech:  " + analysisList.get(0).getGr());
            }
            TreeSet<String> strings = allWordsByTypes.get(partOfSpeech.getStr());
            if(strings == null){
                strings = new TreeSet<>();
                allWordsByTypes.put(partOfSpeech.getStr(), strings);
            }
            strings.add(this.text);
        }
    }

    public static void setPrint() throws IOException {
        // TODO
        String json = new ObjectMapper().writeValueAsString(allWordsByTypes);
        Helper.saveToFile(json, "-" + File.separator + "allWordsByTypes.json");
    }


    public MyStemAnalysis getPopularVariant(){
        if(this.getAnalysisList() != null && this.getAnalysisList().size() > 0)
            return this.getAnalysisList().get(0);
        return null;
    }
}
