package mystem;

import lombok.Getter;
import lombok.Setter;
import utils.Bigram;
import utils.Unigram;

import java.util.*;

@Getter
@Setter
public class MyStemResult {
    public MyStemResult(List<MyStemItem> itemList) {
        this.itemList = itemList;
    }

    private List<MyStemItem> itemList;

    public MyStemResult getOnlyWorlds() {
        List<MyStemItem> result = new ArrayList<>();
        for (MyStemItem myStemItem : this.itemList) {
            if (!myStemItem.isDelimeter())
                result.add(myStemItem);
        }
        return new MyStemResult(result);
    }


    public Map<Bigram, Integer> getBigramFrequensy() {
        List<Bigram> bigramList = new ArrayList<>();

        List<String> tmp = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            MyStemItem myStemItem = itemList.get(i);
            if (myStemItem.isPoint() || i == itemList.size() - 1) {
                for (int j = 0; j < tmp.size() - 1; j++) {
                    bigramList.add(new Bigram(tmp.get(j).toLowerCase(), tmp.get(j + 1).toLowerCase()));
                }
                tmp = new ArrayList<>();
            } else if( !myStemItem.isDelimeter()){
                tmp.add(myStemItem.getBaseForm());
            }
        }
        Map<Bigram, Integer> map = new HashMap<>();
        for (Bigram bigram : bigramList) {
            map.merge(bigram, 1, Integer::sum);
        }
        return map;
    }

    public Map<Unigram, Integer> getUnigramFrequensy(){
        Map<Unigram,Integer> map = new HashMap<>();
        for (MyStemItem myStemItem : itemList) {
            if(!myStemItem.isDelimeter()){
                map.merge(new Unigram(myStemItem.getBaseForm()), 1, Integer::sum);
            }
        }
        return map;
    }
}
