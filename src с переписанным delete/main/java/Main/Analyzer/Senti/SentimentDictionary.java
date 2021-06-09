package Main.Analyzer.Senti;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SentimentDictionary {
    Map<String, SentiValue> map = new HashMap<>();

    public SentimentDictionary() {
    }

    public void addRow(String str, SentiValue sentiValue){
        this.map.put(str, sentiValue);
    }

    public SentiValue getByStr(String str){
        return map.get(str);
    }
}
