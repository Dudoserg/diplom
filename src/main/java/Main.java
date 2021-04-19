import com.fasterxml.jackson.databind.ObjectMapper;
import csv.CSV_DICT;
import data.Reviews;
import dict.DictBase;
import dict.DictException;
import dict.Edge.Edge;
import dict.RelationType;
import dict.Vertex;
import mystem.MyStemItem;
import mystem.MyStemResult;
import mystem.MyStem;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

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
//        reviews.setReview(reviews.getReview().subList(0, 50));
        data = String.join(" ", reviews.getTexts());
        MyStem myStemText = new MyStem(data);

        myStemText.saveToFile(MyStem.FULL_TEXT);
        myStemText = myStemText.removeStopWord();
        myStemText.saveToFile(MyStem.TEXT_WITHOUT_STOPWORDS_txt);
        myStemText.saveToFile("-" + File.separator + "text_withoutStopWord.txt");

        myStemText.lemmatization();
        myStemText.removeStopWordsFromLemmatization();


        Map<Bigram, Integer> bigramFrequensy = myStemText.getMyStemResult().getBigramFrequensy();
        Map<Unigram, Integer> unigramFrequensy = myStemText.getMyStemResult().getUnigramFrequensy();

        Helper.printUnigram(unigramFrequensy, "result" + File.separator + "unigram_frequency.txt");
        Helper.printUnigram(unigramFrequensy, "-" + File.separator + "unigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "result" + File.separator + "bigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "-" + File.separator + "bigram_frequency.txt");

        System.out.print("build dict train...");

        DictBase dictBase = CSV_DICT.loadFullDict();
        dictBase.removeStopWords();

        DictBase dictTrain = new DictBase();
        for (Map.Entry<Bigram, Integer> bigramIntegerEntry : bigramFrequensy.entrySet()) {
            Bigram key = bigramIntegerEntry.getKey();
            Edge edge = dictBase.getEdge(Vertex.getVertex(key.getFirst()), Vertex.getVertex(key.getSecond()));
            if (edge != null)
                dictTrain.addPair(key.getFirst(), key.getSecond(), edge.getWeight(), edge.getRelationType());
            else
                dictTrain.addPair(key.getFirst(), key.getSecond(), Edge.ASS_BASE_WEIGHT, RelationType.ASS);
        }
        dictTrain.removeStopWords();
        dictTrain.printSortedEdge("-" + File.separator + "dictionary_train.txt");
        dictBase.printSortedEdge("-" + File.separator + "_1_dictionary_base.txt");
        System.out.println("\t\t\tdone");

        int _R_ = 3;
        double _GAMMA_ = 0.7;

        DictBase.removeUnusedVertex(dictBase, dictTrain, _R_);
        dictBase.printSortedEdge("-" + File.separator + "_2_dictionary_base after removeUnusedVertex.txt");

        dictBase.correctEdgeWeight(bigramFrequensy, 10, _R_);
        dictBase.printSortedEdge("-" + File.separator + "_3_dictionary_base after correctEdgeWeight.txt");

        dictBase.setVertexWeight(unigramFrequensy);
        dictBase.printSortedVertex("-" + File.separator + "_4_dictionary_base after setVertexWeight.txt");

        dictBase.correctVertexWeight(_R_, _GAMMA_);
        dictBase.printSortedVertex("-" + File.separator + "_5_dictionary_base after correctVertexWeight(r=" +
                _R_+ ",gamma=" + _GAMMA_ + " квадратичное затухание).txt");

        //DictBase.graphviz_graphSaveToFile(DictBase.graphviz_getGraphViz(dictBase), "result\\restaraunt.dot", Format.DOT);
        System.out.println();
    }
}
