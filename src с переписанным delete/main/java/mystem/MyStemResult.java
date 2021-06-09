package mystem;

import lombok.Getter;
import lombok.Setter;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.File;
import java.io.IOException;
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

    public Map<Unigram, Integer> getUnigramFrequensy() {
        Map<Unigram, Integer> map = new HashMap<>();
        for (MyStemItem myStemItem : itemList) {
            if (!myStemItem.isDelimeter()) {
                map.merge(new Unigram(myStemItem.getBaseForm()), 1, Integer::sum);
            }
        }
        return map;
    }

    public void removeStopWords(StopWordsInterface words) {
        List<String> stopWords = words.getStopWords();
        for (int i = itemList.size() - 1; i >= 0; i--) {
            MyStemItem myStemItem = itemList.get(i);
            MyStemAnalysis popularVariant = myStemItem.getPopularVariant();
            if (popularVariant != null) {
                String text = popularVariant.getLex();

                if (stopWords.contains(myStemItem.getText())) {
                    itemList.remove(myStemItem);
                }
            }
        }
    }

    public void saveText() throws IOException {
        String result = "";
        for (MyStemItem myStemItem : this.itemList) {
            if(myStemItem.isPoint())
                result += myStemItem.getText() + "\n";
            else {
                MyStemAnalysis popularVariant = myStemItem.getPopularVariant();
                if (popularVariant != null)
                    result += popularVariant.getLex() + " ";
            }
        }
        Helper.saveToFile(result, "result" + File.separator + "analyzed.txt");
    }


}
