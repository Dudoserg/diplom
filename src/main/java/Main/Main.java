package Main;

import SpellerChecker.Languagetool;
import csv.CSV_DICT;
import data.Reviews;
import dict.*;
import dict.Edge.Edge;
import javafx.util.Pair;
import mystem.MyStem;
import prog2.Sentence;
import prog2.WordOfSentence;
import settings.Settings;
import utils.Bigram;
import utils.Helper;
import utils.Unigram;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
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
//            DictBase testVertex = objectMapper.readValue(str, DictBase.class);
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


        dictBase.correctEdgeWeight(bigramFrequensy, 5, settings.get_R_());
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

        check(dictBase);

        // TODO
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO какой радиус брать инвертированный или нет
        // TODO
        dictBase.distributeVerticesIntoClusters(clastering, 15, settings.get_R_() - 1);
//        dictBase.saveAs("result" + File.separator + "restaurant2_new.dat");

        check(dictBase);

//        Vertex testVertex = dictBase.getVertex("отмечать");
//        Set<Vertex> asdf = dictBase.getInvertMap().keySet();
//        if(asdf.contains(testVertex))
//            System.out.println();

//        DictBase testDict = dictBase.getSubDict(testVertex, 1);
//        testDict.draw("отмечать", Helper.path("result", "test.png"));

        Vertex vertex = dictBase.getVertex("персонал");

        //prog2(dictBase);
        prog22(dictBase);
        System.out.print("");


    }

    public static void prog22(DictBase dictBase) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String s = Helper.readFile(Helper.path("data", "semeval", "restaurant", "test", "test.txt"));
        Languagetool languagetool = new Languagetool();
        s = languagetool.getCorrect(s);
        System.out.println(s);

        MyStem myStem = new MyStem(s, UUID.randomUUID().toString());
        myStem = myStem.removeStopWord();
        myStem.lemmatization();
        myStem.removeStopWordsFromLemmatization();
        myStem.removeTmpFiles();


        List<Sentence> sentencesList2 = myStem.getSentencesList2();

        // слово  - соответствующая ему вершина в графе
        Map<String, Vertex> map = dictBase.getStringVertexMap();

        // цикл по предложениям обрабатываемого текста
        for (Sentence sentence : sentencesList2) {
            // список слов предложения приведенных к лексической форме
            List<WordOfSentence> wordOfSentenceList = sentence.getWordOfSentenceList();

            // перебираем слова из предложения, помечаем в какие кластера отностися каждое из слов
            for (WordOfSentence wordOfSentence : wordOfSentenceList) {
                // Находим вершину из графа соответствующей текущему слову
                Vertex vertex = map.get(wordOfSentence.getWord());
                wordOfSentence.setVertex(vertex);
                if (vertex == null) {
                    // такого слова в графе нет, завершаем обработку слова
                    continue;
                }

                // слово может относиться к нескольким кластерам, перебираем каждый из кластеров
                for (Pair<Cluster, Integer> clusterIntegerPair : vertex.getShortest()) {
                    wordOfSentence.addCluster(
                            clusterIntegerPair.getKey(),
                            clusterIntegerPair.getValue()
                    );
                }

                /////************************************
//                System.out.println(wordOfSentence.getWord());
//                wordOfSentence.getFunc().entrySet().stream()
//                        .sorted((o1, o2) -> Integer.compare(o1.getValue(), o2.getValue()))
//                        .forEach(elem ->
//                                System.out.println("\t" + elem.getKey().getVertex().getWord().getStr() + "\t" + elem.getValue())
//                        );
                /////************************************
            }

            Map<Cluster, Double> result = dictBase.getClusterList().stream().collect(Collectors.toMap(
                    cluster -> cluster,
                    cluster -> 0.0
            ));
            // опять перебираем все слова, и считаем характеристические функции принадлежности кластеру
            for (WordOfSentence wordOfSentence : wordOfSentenceList) {
                if (wordOfSentence.getVertex() == null)
                    continue;
                for (Map.Entry<Cluster, Integer> clusterIntegerEntry : wordOfSentence.getFunc().entrySet()) {
                    Cluster keyCluster = clusterIntegerEntry.getKey();
                    Integer valueDistance = clusterIntegerEntry.getValue();
                    double coeff = 0;   // коэффициент затухания
                    if (valueDistance == -1) {
                        // данное слово является центром кластера
                        coeff = 1;
                    } else {
                        // 0 - одна дуга до центра => первая степень затухания
                        // 1 - две дуги до центра => вторая степень затухания
                        coeff = Math.pow(0.8, valueDistance + 1);
                    }
                    // вес вершины в кластере
//                    double weight = keyCluster.getVertex().getWeight();
                    double weight = wordOfSentence.getVertex().getWeight();
                    // влияние вершины на характеристическую функцию принадлежности предложения к кластеру
                    double wordValue = weight * coeff;

                    result.put(keyCluster, result.get(keyCluster) + wordValue);
                }
            }

//            for (Map.Entry<Cluster, Double> clusterDoubleEntry : result.entrySet()) {
//                Cluster key = clusterDoubleEntry.getKey();
//                Double oldValue = clusterDoubleEntry.getValue();
//                Double newValue =((Math.log(key.getWeight()) / Math.log(2))) / key.getWeight() * oldValue;
//                result.put(key, newValue);
//            }


            List<Map.Entry<Cluster, Double>> sortedResult = new ArrayList<>(result.entrySet());
            sortedResult.sort((first, second) -> -Double.compare(first.getValue(), second.getValue()));

            System.out.println("=======================================================================================");
            System.out.println(sentence.getProcessedText());
            for (int i = 0; i < 5; i++) {
                Map.Entry<Cluster, Double> elem = sortedResult.get(i);
                System.out.println("\t" + elem.getKey().getVertex().getWord().getStr() + "\t"
                        + elem.getValue());
            }
            System.out.println();
        }
        System.out.println("prog 22 time = " + (System.currentTimeMillis() - startTime) + " ms.");

    }

    public static void prog2(DictBase dictBase) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String s = Helper.readFile(Helper.path("data", "semeval", "restaurant", "test", "test.txt"));
        Languagetool languagetool = new Languagetool();
        s = languagetool.getCorrect(s);
        System.out.println(s);

        MyStem myStem = new MyStem(s, UUID.randomUUID().toString());
        myStem = myStem.removeStopWord();
        myStem.lemmatization();
        myStem.removeStopWordsFromLemmatization();
        myStem.removeTmpFiles();
        List<List<String>> sentencesList = myStem.getSentencesList();


        Map<String, Vertex> map = dictBase.getStringVertexMap();

        // перебираем все предложения оцениваемого отзыва
        for (List<String> strings : sentencesList) {
            // Список слов текущего предложения
            List<WordOfSentence> wordList = new ArrayList<>();
            for (String string : strings) {
                wordList.add(new WordOfSentence(string, null));
            }

            // перебираем слова из оцениваемого предложения
            for (WordOfSentence word : wordList) {
                // находим соответствующую слову из оцениваемого предложения, вершину из графа
                Vertex vertex = map.get(word.getWord());
                word.setVertex(vertex);
                if (vertex == null)
                    continue;

                /////////////////////
                System.out.println(vertex.getWord().getStr());
                for (Pair<Cluster, Integer> clusterIntegerPair : vertex.getShortest()) {
                    System.out.println("\t" + clusterIntegerPair.getKey().getVertex().getWord().getStr() + "\t" + clusterIntegerPair.getValue());
                }
                System.out.println();
                /////////////////////


                // все кластеры, в которые входит данное слово из графа, помещаем в объект-слово
                if (vertex != null && vertex.getShortest() != null && vertex.getShortest().size() > 0) {
                    for (Pair<Cluster, Integer> clusterIntegerPair : vertex.getShortest()) {
                        Cluster cluster = clusterIntegerPair.getKey();
                        Integer value = clusterIntegerPair.getValue();
                        word.getFunc().put(cluster, value);
                    }
                }
            }
            System.out.print("");
            // перебираем все слова из предложения, и считаем по формуле характеристику - принадлежности
            // всего предложения к каждому из кластеров
            List<Cluster> clusterList = dictBase.getClusterList();
            Map<Cluster, Double> funct = new HashMap<>();
            for (Cluster cluster : clusterList) {
                funct.put(cluster, 0.0);
            }

            for (WordOfSentence wordOfSentence : wordList) {
                for (Map.Entry<Cluster, Integer> clusterDoubleEntry : wordOfSentence.getFunc().entrySet()) {
                    Cluster key = clusterDoubleEntry.getKey();
                    Integer distance = clusterDoubleEntry.getValue();
                    double tmp = wordOfSentence.getVertex().getWeight();
                    if (distance != -1) {
                        double c = Math.pow(0.8, distance + 1);
                        tmp *= c;
                    }
                    funct.put(key, funct.get(key) + tmp);
                }
            }
            // Умножаем на логарифм
//            for (Map.Entry<Cluster, Double> clusterDoubleEntry : funct.entrySet()) {
//                Cluster cluster = clusterDoubleEntry.getKey();
//                Double value = clusterDoubleEntry.getValue();
//                double coeff = (Math.log(cluster.getWeight()) / Math.log(2)) / cluster.getWeight();
//                funct.put(cluster,  value * coeff);
//            }

            List<Map.Entry<Cluster, Double>> sortedResult = new ArrayList<>(funct.entrySet());
            sortedResult.sort((first, second) -> -Double.compare(first.getValue(), second.getValue()));

            System.out.println(
                    wordList.stream()
                            .map(wordOfSentence -> wordOfSentence.getWord())
                            .collect(Collectors.joining(" "))
            );

            for (int i = 0; i < 5; i++) {
                Map.Entry<Cluster, Double> clusterDoubleEntry = sortedResult.get(i);
                String str = clusterDoubleEntry.getKey().getVertex().getWord().getStr();
                System.out.println("\t" + str + "\t" + clusterDoubleEntry.getValue());
            }
            System.out.println();
            System.out.print("");

        }
        System.out.println("prog 2 time = " + (System.currentTimeMillis() - startTime) + " ms.");
    }



    public static void check(DictBase dictBase) throws IOException {
        if (true)
            return;
        List<ClusterHelper> clastering = dictBase.clastering(1, 0.1);

        //System.out.println("==============================================================================");

        int tmpIndex = 0;
        for (ClusterHelper claster : clastering) {
            if (claster.getVertex().isNoun())
                tmpIndex++;
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
    }
}
