package Main;

import com.fasterxml.jackson.databind.ObjectMapper;
import csv.CSV_DICT;
import data.Reviews;
import dict.*;
import dict.Edge.Edge;
import mystem.MyStem;
import mystem.MyStemItem;
import settings.Settings;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        test();
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

        // TODO включить обратно)0
        //dictBase.removeStopWords();

        DictBase dictTrain = new DictBase();
        for (Map.Entry<Bigram, Integer> bigramIntegerEntry : bigramFrequensy.entrySet()) {
            Bigram key = bigramIntegerEntry.getKey();
            Edge edge = dictBase.getEdge(Vertex.getVertex(dictBase, key.getFirst()), Vertex.getVertex(dictBase, key.getSecond()));
            if (edge != null)
                dictTrain.addPair(key.getFirst(), key.getSecond(), edge.getWeight(), edge.getRelationType());
            else
                dictTrain.addPair(key.getFirst(), key.getSecond(), Edge.ASS_BASE_WEIGHT, RelationType.ASS);
        }
        //dictTrain.removeStopWords();
        dictTrain.printSortedEdge("-" + File.separator + "_1_dict__train.txt");
        dictBase.printSortedEdge("-" + File.separator + "_1_dict__base.txt");
        System.out.println("\t\t\tdone");

        // TODO
//        DictBase.removeUnusedVertex(dictBase, dictTrain, settings.get_R_());
//        dictBase.printSortedEdge("-" + File.separator + "_2_dict__base after removeUnusedVertex" +
//                settings.getSettings() + ".txt");


        dictBase.correctEdgeWeight(bigramFrequensy, 10, settings.get_R_());
        dictBase.printSortedEdge("-" + File.separator + "_3_dict__base after correctEdgeWeight" +
                settings.getSettings() + ".txt");


        dictBase.setVertexWeight(unigramFrequensy);
        dictBase.printSortedVertex("-" + File.separator + "_4_dict__base after setVertexWeight" +
                settings.getSettings() + ".txt");


        //dictBase.saveAs("result" + File.separator + "restaurant.dat");


        dictBase.correctVertexWeight(settings.get_R_(), settings.get_GAMMA_(), settings.get_GAMMA_ATTENUATION_RATE_(), true);
        dictBase.printSortedVertex("-" + File.separator + "_5_dict__base after correctVertexWeight" +
                settings.getSettings() + ".txt");
        dictBase.printSortedVertex("-" + File.separator + "_5_dict__base NOUN after correctVertexWeight" +
                settings.getSettings() + ".txt", PartOfSpeech.NOUN, 100);


        dictBase.calculateWeightOfOutgoingVertex();
        List<ClusterHelper> clastering = dictBase.clastering(1, 0.1);

        System.out.println("==============================================================================");
//        int tmpIndex = 0;
//        for (ClusterHelper claster : clastering) {
//            if (claster.getVertex().isAdjective())
//                System.out.println((tmpIndex++) + ") " + claster.getVertex().getWord().getStr() + "\t" + "w(вершины)=" +
//                        claster.getVertex().getWeight() + "\t" + "w(соседи)=" + claster.getVertex().getWeightOutgoingVertex() +
//                        "\t" + "w(кластера)=" + claster.getClusterWeight());
//            if (tmpIndex > 40)
//                break;
//        }
        dictBase.saveTopClusters(clastering, settings.getSettings());
        dictBase.assignVertexToClusters(clastering, settings.get_R_());

        int c = 0;
        int count = 0;
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
            c++;
            Vertex key = vertexEdgeMapEntry.getKey();
            if (key.getIncludedInClusters().size() > 1) {
                count++;
                System.out.println(key.getWord().getStr() + "\t" + "[" +
                        key.getIncludedInClusters().stream()
                                .map(clusterIntegerPair ->
                                        clusterIntegerPair.getKey().getCenter().getWord().getStr() + "(" + clusterIntegerPair.getValue() + ")"
                                ).collect(Collectors.joining(", ")) + "]"
                );
            }
        }

        String inter = "интерьер";
        String disign = "дизайн";

        {
            EdgeMap интерьер = dictBase.getMap().get(Vertex.getVertex(dictBase, inter));
            System.out.println(inter);
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : интерьер.getEdgeMap().entrySet()) {
                System.out.println("\t" + vertexEdgeEntry.getKey().getWord().getStr());
            }
            EdgeMap _интерьер = dictBase.getInvertMap().get(Vertex.getVertex(dictBase, inter));
            System.out.println("_интерьер");
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : _интерьер.getEdgeMap().entrySet()) {
                System.out.println("\t" + vertexEdgeEntry.getKey().getWord().getStr());
            }

            System.out.println();
            EdgeMap дизайн = dictBase.getMap().get(Vertex.getVertex(dictBase, disign));
            System.out.println(disign);
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : дизайн.getEdgeMap().entrySet()) {
                System.out.println("\t" + vertexEdgeEntry.getKey().getWord().getStr());
            }
            EdgeMap _дизайн = dictBase.getInvertMap().get(Vertex.getVertex(dictBase, disign));
            System.out.println("_дизайн");
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : _дизайн.getEdgeMap().entrySet()) {
                System.out.println("\t" + vertexEdgeEntry.getKey().getWord().getStr());
            }
        }
        System.out.println("------");
//        {
//            EdgeMap интерьер = интерьер1.getMap().get(Vertex.getVertex(интерьер1, inter));
//            System.out.println(inter);
//            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : интерьер.getEdgeMap().entrySet()) {
//                System.out.println("\t" + vertexEdgeEntry.getKey().getWord().getStr());
//            }
//            EdgeMap _интерьер = интерьер1.getInvertMap().get(Vertex.getVertex(интерьер1, inter));
//            System.out.println("_интерьер");
//            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : _интерьер.getEdgeMap().entrySet()) {
//                System.out.println("\t" + vertexEdgeEntry.getKey().getWord().getStr());
//            }
//        }


        DictBase интерьер1 = dictBase.getFullSubDict(Vertex.getVertex(dictBase, "форма"), 0);
        DictBase.graphviz_draw(DictBase.graphviz_getGraphViz(интерьер1), "result" + File.separator + "r1.png");
        int size1 = интерьер1.getInvertMap().size();


        DictBase интерьер2 = dictBase.getFullSubDict(Vertex.getVertex(dictBase, "форма"), 1);
        DictBase.graphviz_drawHight(DictBase.graphviz_getGraphViz(интерьер2), "result" + File.separator + "r2.png");
        int size2 = интерьер2.getInvertMap().size();



    }

    static void test() throws IOException, InterruptedException {
        MyStem myStem = new MyStem(Helper.readFileLineByLine(Helper.path("data", "semeval", "restaurant", "test", "test.txt")), "test");
        myStem = myStem.removeStopWord();
        myStem.lemmatization();
        for (MyStemItem item : myStem.getMyStemResult().getItemList()) {
            String lex = item.getAnalysisList().get(0).getLex();
        }
        System.out.println();
    }
}
