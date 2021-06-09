package Ngrams;

import mystem.MyStemItem;
import mystem.MyStemResult;
import utils.Bigram;
import utils.Unigram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ngrams implements NgramsInt{
    @Override
    public Map<Bigram, Integer> getBigramFrequensy(List<MyStemItem> itemList) {
        List<Bigram> bigramList = new ArrayList<>();

        List<String> tmp = new ArrayList<>();
        for (int i = 0; i < itemList.size(); i++) {
            MyStemItem myStemItem = itemList.get(i);
            //System.out.println(myStemItem.getText());
            int asdf = 0;
            if (myStemItem.isPoint() || i == itemList.size() - 1) {
                //System.out.println("=====");
                for (int j = 0; j < tmp.size() - 1; j++) {
                    bigramList.add(new Bigram(tmp.get(j).toLowerCase(), tmp.get(j + 1).toLowerCase()));
                    //System.out.println(bigramList.get(bigramList.size() - 1).getFirst() + "\t" + bigramList.get(bigramList.size() - 1).getSecond());
                    //if("оставаться".equals(bigramList.get(bigramList.size() - 1).getFirst())
                    // && "довольный".equals(bigramList.get(bigramList.size() - 1).getSecond())){
                    //    asdf++;
                    //}
                }
                tmp = new ArrayList<>();
                //System.out.println("=============================================");
            } else if (!myStemItem.isDelimeter()) {
                tmp.add(myStemItem.getBaseForm());
            }
        }
        Map<Bigram, Integer> map = new HashMap<>();
        for (Bigram bigram : bigramList) {
            map.merge(bigram, 1, Integer::sum);
        }
        return map;
    }

    @Override
    public Map<Unigram, Integer> getUnigramFrequensy(List<MyStemItem> itemList) {
        Map<Unigram, Integer> map = new HashMap<>();
        for (MyStemItem myStemItem : itemList) {
            if (!myStemItem.isDelimeter()) {
                map.merge(new Unigram(myStemItem.getBaseForm()), 1, Integer::sum);
            }
        }
        return map;
    }
}
