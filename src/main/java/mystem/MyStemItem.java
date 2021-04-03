package mystem;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MyStemItem {
    @JsonProperty("analysis")
    private List<MyStemAnalysis> analysisList;
    private String text;

    /**
     * Является ли этот айтем разделителем
     * @return
     */
    public boolean isDelimeter(){
        return analysisList == null || analysisList.size() == 0;
    }

    public boolean isPoint(){
        return text.trim().equals(".");
    }

    public String getBaseForm(){
        return analysisList.get(0).getLex();
    }
}
