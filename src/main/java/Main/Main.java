package Main;

import csv.CSV_DICT;
import data.Reviews;
import dict.*;
import dict.Edge.Edge;
import mystem.MyStem;
import settings.Settings;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws Exception {
/*        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        double x = 0.95;

        for(int i = 0 ; i < 5 ; i++){
            System.out.print(decimalFormat.format(x) + "  ");
            x = x * x;
        }
        System.out.print("");

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

//        {
//            ObjectMapper objectMapper = new ObjectMapper();
//            String str = Helper.readFile("result" + File.separator + "dict.json");
//            DictBase ss = objectMapper.readValue(str, DictBase.class);
//            System.out.println();
//        }


        settings = new Settings(
                0.6, 0.3, 0.2, 3, 0.65, 3
        );

        Reviews reviews = Reviews.readFromFile(Reviews.RU_TRAIN_PATH);
        String data = String.join(" ", reviews.getTexts());

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


        DictBase dictBase = CSV_DICT.loadFullDict();

        //dictBase.bidirectional(RelationType.ASS);///////////////////////////////////////////////////////////////////////

        dictBase.removeStopWords();

        DictBase dictTrain = new DictBase();
        for (Map.Entry<Bigram, Integer> bigramIntegerEntry : bigramFrequensy.entrySet()) {
            Bigram key = bigramIntegerEntry.getKey();
            Edge edge = dictBase.getEdge(Vertex.getVertex(dictBase, key.getFirst()), Vertex.getVertex(dictBase, key.getSecond()));

//            Vertex vertexFrom = dictTrain.getVertex(key.getFirst());
//            Vertex vertexTo = dictTrain.getVertex(key.getSecond());

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


        //dictBase.saveAs("result" + File.separator + "restaurant.dat");


        dictBase.correctVertexWeight(settings.get_R_(), settings.get_GAMMA_(), settings.get_GAMMA_ATTENUATION_RATE_(), true);
        dictBase.printSortedVertex("-" + File.separator + "_5_dictionary_base after correctVertexWeight(r=" +
                settings.get_R_() + ",gamma=" + settings.get_GAMMA_() + " 3 затухание).txt");
        dictBase.printSortedVertex("-" + File.separator + "_5_dictionary_base NOUN after correctVertexWeight(r=" +
                settings.get_R_() + ",gamma=" + settings.get_GAMMA_() + " 3 затухание).txt", PartOfSpeech.NOUN, 100);

        //DictBase.graphviz_graphSaveToFile(DictBase.graphviz_getGraphViz(dictBase), "result\\restaraunt.dot", Format.DOT);


        dictBase.calculateWeightOfOutgoingVertex();
        List<ClusterHelper> clastering = dictBase.clastering(1, 0.1);

        System.out.println("==============================================================================");

        int tmpIndex = 0;
        for (ClusterHelper claster : clastering) {
            if (claster.getVertex().isNoun())
                System.out.println((tmpIndex++) + ") " + claster.getVertex().getWord().getStr() + "\t" + "w=" +
                        claster.getVertex().getWeight() + "\t" + "wO=" + claster.getVertex().getWeightOutgoingVertex() +
                        "\t" + "wC=" + claster.getClusterWeight());
            if (tmpIndex > 40)
                break;
        }

        if (!"отмечать".equals(clastering.get(20).getVertex().getWord().getStr()) ||
                Math.abs(clastering.get(20).getVertex().getWeight() - 259.14787329) > 0.001 ||
                !"салат".equals(clastering.get(13).getVertex().getWord().getStr()) ||
                Math.abs(clastering.get(13).getVertex().getWeight() - 242.0) > 0.001 ||
                !"зал".equals(clastering.get(33).getVertex().getWord().getStr()) ||
                Math.abs(clastering.get(33).getVertex().getWeightOutgoingVertex() - 657.44986) > 0.001) {
            throw new IOException("ЕЕЕЕРРРРРРОООООРРРРР");
        }
        //Vertex selezenVertex = dictBase.getVertex("селезень");
        //List<HashSet<Vertex>> vertexInRadiuses = dictBase.findVertexInRadiuses(selezenVertex);

//        DictBase selezen = dictBase.getFullSubDict(selezenVertex, 1);
//        DictBase.graphviz_drawHight(DictBase.graphviz_getGraphViz(selezen), "result" + File.separator + "s1.png");

        Vertex eda = dictBase.getVertex("еда");
        DictBase subDict = dictBase.getSubDict(eda, settings.get_R_());
        DictBase invertSubDict = dictBase.getInvertSubDict(eda, settings.get_R_());
        DictBase fullSubDict = dictBase.getFullSubDict(eda, settings.get_R_());

//        dictBase.saveAs("result" + File.separator + "restaurant.dat");
        dictBase.distributeVerticesIntoClusters(clastering, settings.get_R_());
        Vertex спорный = dictBase.getVertex("спорный");
        dictBase.saveAs("result" + File.separator + "restaurant2_new.dat");


         System.out.print("");
    }
}
