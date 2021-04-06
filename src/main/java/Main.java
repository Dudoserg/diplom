import com.fasterxml.jackson.databind.ObjectMapper;
import mystem.MyStemItem;
import mystem.MyStemResult;
import mystem.MyStemText;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
//        long l = System.currentTimeMillis();
//        Map<Word, Word> map = new HashMap<>();
//        TreeSet<Integer> treeSet = new TreeSet<>();
//        for (int i = 0; i < 2*1000*1000; i++) {
//            Word word = new Word("hello" + i);
//            map.put(word, new Word("hello"));
//            treeSet.add(word.hashCode());
////            if(i % (10*1000) == 0)
////                System.out.println(i);
//        }
//
//        Word hello = map.get(new Word("hello"));
//
//        System.out.println( (System.currentTimeMillis() - l));

        MyStemText myStemText = new MyStemText(Helper.readFile("mystem" + File.separator + "data.txt"));
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
        MyStemResult onlyWorlds = myStemResult.getOnlyWorlds();
        Map<Bigram, Integer> bigramFrequensy = myStemResult.getBigramFrequensy();
        Map<Unigram, Integer> unigramFrequensy = myStemResult.getUnigramFrequensy();
        System.out.printf("");
        Helper.printUnigram(unigramFrequensy, "result" + File.separator + "unigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "result" + File.separator + "bigram_frequency.txt");
//        Start start = new Start();
    }
}
