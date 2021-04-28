package Main;

import csv.CSV_DICT;
import data.Reviews;
import dict.*;
import dict.Edge.Edge;
import javafx.util.Pair;
import mystem.MyStem;
import settings.Settings;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static Settings settings;

    public static void mystemTest() throws IOException, DictException, InterruptedException {

        settings = new Settings(
                0.6, 0.3, 0.2, 3, 0.7, 3
        );

        String data = "";
        //data = Helper.readFile("mystem" + File.separator + "data.txt");
        Reviews reviews = Reviews.readFromFile(Reviews.RU_TRAIN_PATH);
//        reviews.setReview(reviews.getReview().subList(0, 50));
        data = String.join(" ", reviews.getTexts());
        MyStem myStemText = new MyStem(data, "tt_");

        myStemText.saveToFile(MyStem.FULL_TEXT);
        myStemText = myStemText.removeStopWord();
        myStemText.saveToFile(MyStem.TEXT_WITHOUT_STOPWORDS_txt);
        myStemText.saveToFile("-" + File.separator + "text_withoutStopWord.txt");

        myStemText.lemmatization();
        myStemText.removeStopWordsFromLemmatization();


        Map<Bigram, Integer> bigramFrequensy = myStemText.getMyStemResult().getBigramFrequensy();
        Map<Unigram, Integer> unigramFrequensy = myStemText.getMyStemResult().getUnigramFrequensy();

        Helper.printUnigram(unigramFrequensy, "result" + File.separator + "unigram_frequency.txt");
        Helper.printUnigram(unigramFrequensy, "-" + File.separator + "_0_unigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "result" + File.separator + "bigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "-" + File.separator + "_0_bigram_frequency.txt");

        System.out.print("build dict train...");

        DictBase dictBase = CSV_DICT.loadFullDict();
        //dictBase.bidirectional(RelationType.ASS);///////////////////////////////////////////////////////////////////////
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
        dictTrain.printSortedEdge("-" + File.separator + "_1_dictionary_train.txt");
        dictBase.printSortedEdge("-" + File.separator + "_1_dictionary_base.txt");
        System.out.println("\t\t\tdone");

//        {
//            DictBase dishes = dictBase.getSubDict(Vertex.getVertex("блюдо"), 1);
//            DictBase.graphviz_draw(DictBase.graphviz_getGraphViz(dishes), "-"  + File.separator + "dishes.jpg");
//
//            DictBase dishes_invert = dictBase.getInvertSubDict(Vertex.getVertex("блюдо"), 1);
//            DictBase.graphviz_draw(DictBase.graphviz_getGraphViz(dishes_invert), "-"  + File.separator + "dishes_invert.jpg");
//
//            DictBase both = DictBase.createFromDicts(dishes, dishes_invert);
//            DictBase.graphviz_draw(DictBase.graphviz_getGraphViz(both), "-"  + File.separator + "both.jpg");
//        }

        DictBase.removeUnusedVertex(dictBase, dictTrain, settings.get_R_());


        dictBase.printSortedEdge("-" + File.separator + "_2_dictionary_base after removeUnusedVertex.txt");

        dictBase.correctEdgeWeight(bigramFrequensy, 10, settings.get_R_());
        dictBase.printSortedEdge("-" + File.separator + "_3_dictionary_base after correctEdgeWeight.txt");

        dictBase.setVertexWeight(unigramFrequensy);
        dictBase.printSortedVertex("-" + File.separator + "_4_dictionary_base after setVertexWeight.txt");

        dictBase.correctVertexWeight(settings.get_R_(), settings.get_GAMMA_(), settings.get_GAMMA_ATTENUATION_RATE_(), true);

        dictBase.printSortedVertex("-" + File.separator + "_5_dictionary_base after correctVertexWeight(r=" +
                settings.get_R_() + ",gamma=" + settings.get_GAMMA_() + " 3 затухание).txt");
        dictBase.printSortedVertex("-" + File.separator + "_5_dictionary_base NOUN after correctVertexWeight(r=" +
                settings.get_R_() + ",gamma=" + settings.get_GAMMA_() + " 3 затухание).txt", PartOfSpeech.NOUN, 100);

        //DictBase.graphviz_graphSaveToFile(DictBase.graphviz_getGraphViz(dictBase), "result\\restaraunt.dot", Format.DOT);

        dictBase.calculateWeightOfOutgoingVertex();
        List<ClusterHelper> clastering = dictBase.clastering(10, 5);
        System.out.println("==============================================================================");

        int tmpIndex = 0;
        for (ClusterHelper claster : clastering) {
            if (claster.getVertex().isNoun())
                System.out.println((tmpIndex++) + ") " + claster.getVertex().getWord().getStr() + "\t" + "w=" + claster.getVertex().getWeight() + "\t"
                        + "wO=" + claster.getVertex().getWeightOutgoingVertex() + "\t" + "wC=" + claster.getClusterWeight());
        }

        System.out.println();

//        for (Pair<Vertex, Double> vertexDoublePair : clastering) {
//            Vertex vertex = vertexDoublePair.getKey();
//            Double value = vertexDoublePair.getValue();
//            System.out.print(vertex.getWord().getStr() + "\t" + vertex.getWeight() + "\t" + value + "\t" + vertex.getWeightOutgoingVertex());
//            System.out.println();
//        }
//
//
//
//        System.out.println("==============================================================================");
//        int count = 0;
//        for (int i = 0; i < 200; i++) {
//            if (PartOfSpeech.NOUN.equals(clastering.get(i).getKey().getWord().getPartOfSpeech())) {
//                if(clastering.get(i).getKey().getWeight() > 100){
//                    count++;
//                    System.out.println(clastering.get(i).getKey().getWord().getStr() + "\t" + clastering.get(i).getValue());
//                }
//            }
//            if (count > 30)
//                break;
//        }
//        System.out.println();

    }
}
