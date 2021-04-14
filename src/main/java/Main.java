import com.fasterxml.jackson.databind.ObjectMapper;
import csv.CSV_DICT;
import data.Reviews;
import dict.DictBase;
import dict.DictException;
import dict.Edge.Edge;
import dict.RelationType;
import mystem.MyStemItem;
import mystem.MyStemResult;
import mystem.MyStemText;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws Exception {
/*        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        double x = 0.95;

        for(int i = 0 ; i < 5 ; i++){
            System.out.print(decimalFormat.format(x) + "  ");
            x = x * x;
        }
        System.out.println();

        x = 0.95;
        for(int i = 0 ; i < 5 ; i++){
            System.out.print(decimalFormat.format(x) + "  ");
            x = x * x * x;
        }
        if(true)
            return;*/
//        long l = System.currentTimeMillis();
//        Map<dict.Word, dict.Word> map = new HashMap<>();
//        TreeSet<Integer> treeSet = new TreeSet<>();
//        for (int i = 0; i < 2*1000*1000; i++) {
//            dict.Word word = new dict.Word("hello" + i);
//            map.put(word, new dict.Word("hello"));
//            treeSet.add(word.hashCode());
////            if(i % (10*1000) == 0)
////                System.out.println(i);
//        }
//
//        dict.Word hello = map.get(new dict.Word("hello"));
//
//        System.out.println( (System.currentTimeMillis() - l));

        //Start start = new Start();
        mystemTest();
    }

    public static void mystemTest() throws IOException, DictException {
        String data = "";
        //data = Helper.readFile("mystem" + File.separator + "data.txt");
        Reviews reviews = Reviews.readFromFile(Reviews.RU_TRAIN_PATH);
//        reviews.setReview(reviews.getReview().subList(0, 5));
        data = String.join(" ", reviews.getTexts());
        MyStemText myStemText = new MyStemText(data);
        myStemText = myStemText.removeStopWord();
        myStemText.saveToFile("mystem" + File.separator + Helper.TEXT_WITHOUT_STOPWORDS_txt);

        String myStemPath = "mystem" + File.separator + Helper.MYSTEM_exe;
        String filePath = "mystem" + File.separator + Helper.TEXT_WITHOUT_STOPWORDS_txt;

        String resultFilePath = "mystem" + File.separator + Helper.MYSTEM_RESULT_json;
        try {
            Process p = Runtime.getRuntime()
                    .exec(myStemPath + " " + filePath + " " + resultFilePath + " " + "--format json -c -l -s -i");
            p.waitFor();
        } catch (Exception ex) {
            System.out.println("exception is:" + ex);
        }

        String json = Helper.readFile(resultFilePath);
        ObjectMapper objectMapper = new ObjectMapper();


        MyStemResult myStemResult = new MyStemResult(Arrays.asList(objectMapper.readValue(json, MyStemItem[].class)));
        //MyStemResult onlyWorlds = myStemResult.getOnlyWorlds();
        //for (MyStemItem myStemItem : onlyWorlds.getItemList()) {
          //  System.out.println(myStemItem.getAnalysisList().get(0).getLex());
        //}
        Map<Bigram, Integer> bigramFrequensy = myStemResult.getBigramFrequensy();
        Map<Unigram, Integer> unigramFrequensy = myStemResult.getUnigramFrequensy();

        Helper.printUnigram(unigramFrequensy, "result" + File.separator + "unigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "result" + File.separator + "bigram_frequency.txt");

        DictBase dictTrain = new DictBase();
        for (Map.Entry<Bigram, Integer> bigramIntegerEntry : bigramFrequensy.entrySet()) {
            Bigram key = bigramIntegerEntry.getKey();
            dictTrain.addPair(key.getFirst(), key.getSecond(), Edge.ASS_BASE_WEIGHT, RelationType.ASS);
        }

        DictBase dictBase = CSV_DICT.loadFullDict();
        dictBase.removeUnusedVertex(dictBase, dictTrain, 5);
        System.out.println();
    }
}
