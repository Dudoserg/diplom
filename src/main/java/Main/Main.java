package Main;

import SpellerChecker.Languagetool;
import csv.CSV_DICT;
import data.Reviews;
import dict.*;
import dict.CorrectVertexWeight.CorrectVertexWeight;
import dict.CorrectVertexWeight.CorrectVertexWeightInterface;
import dict.Edge.Edge;
import dict.ModificateEdge.ModificateEdgeInterface;
import dict.ModificateEdge.ModificateEdgeInterfaceImpl;
import dict.SetVertexWeight.SetVertexWeight;
import dict.SetVertexWeight.SetVertexWeightInterface;
import javafx.util.Pair;
import mystem.*;
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
    static Integer start;
    static Integer finish;
    public static Settings settings;

    public static void main(String[] args) throws Exception {
//        if(args.length == 0){
//            settings.loadDefault();
//        }
//        start = Integer.valueOf(args[0]);
//        finish = Integer.valueOf(args[1]);
//
//        System.out.println(" start = " + start + "\t" + finish);
//
//        System.out.println("Поехали?");
//        System.in.read();

        perfomanceTest();
//        correctingDictionary();
//        start();
    }

    private static void correctingDictionary() throws IOException, DictException, InterruptedException {
        settings = new Settings(
//                0.05, 0.05, 0.15, 3, 0.65, 3
                0.6, 0.3, 0.2, 3, 0.65, 3
        );
        DictBase dictBase = CSV_DICT.loadFullDict();
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            stringBuilder.append(key.getWord().getStr() + "\n");
        }
        Mystem mystem = new Mystem();
        MyStemResult analyze = mystem.analyze(stringBuilder.toString());
        int count = 0;
        Map<String, String> mapForSwap = new HashMap<>();
        for (MyStemItem myStemItem : analyze.getItemList()) {
            String firstAnalyseOrText = myStemItem.getFirstAnalyseOrText();
            String before = myStemItem.getText();
            String after = firstAnalyseOrText.replace("\n", "");
            String result = before + "\t\t" + after;
            if (!before.equals(" ") && !after.equals(" ")) {

                if (!before.equals(after)) {
                    mapForSwap.put(before, after);
                    System.out.println(result);
                    count++;
                } else {
                    // System.out.println(result);
                }
            }
        }
        System.out.println("\n\n" + "count = " + count);

        // читаем теперь словарь, и меняем все неправильные глаголы К НОРМАЛЬНОЙ БАЗОВОЙ ФОРМЕ
        String pathDictionary = "data" + File.separator + "connections2.csv";
        String pathCorrectDictionary = "data" + File.separator + "connections3.csv";
        List<String> strings = Helper.readFileLineByLine(pathDictionary);
        String rowWithColumnTitle = strings.get(0);
        strings.remove(0);

        StringBuilder correctDictionaryBuilder = new StringBuilder();
        correctDictionaryBuilder.append(rowWithColumnTitle + "\n");
        for (String string : strings) {
            String[] split = string.split(";");
            String id = split[0];
            String firstPartOfSpeech = split[1];
            String secondPartOfSpeech = split[2];
            String relationType = split[3];
            String firstWord = split[4];
            String secondWord = split[5];

            String rightFirstWord = mapForSwap.get(firstWord);
            if (rightFirstWord != null) {
                firstWord = rightFirstWord;
            }

            String rightSecondWord = mapForSwap.get(secondWord);
            if (rightSecondWord != null) {
                secondWord = rightSecondWord;
            }
            String result = id + ";" + firstPartOfSpeech + ";" + secondPartOfSpeech + ";" + relationType +
                    ";" + firstWord + ";" + secondWord;
            System.out.println("==========================================");
            System.out.println(string);
            System.out.println(result);
            correctDictionaryBuilder.append(result + "\n");
        }
        Helper.saveToFile(correctDictionaryBuilder.toString(), pathCorrectDictionary);
    }

    private static void perfomanceTest() throws IOException, DictException, InterruptedException {

        Map<Bigram, Integer> bigramFrequensy;
        Map<Unigram, Integer> unigramFrequensy;

        boolean isNew = false;
        if (isNew) {
            Reviews reviews = Reviews.readFromFile(Reviews.RU_TRAIN_PATH);
            String data = String.join(" ", reviews.getTexts());
            Mystem mystem = new Mystem();
            MyStemResult myStemResult = mystem.analyze(data);

            myStemResult.removeStopWords(StopWords.getInstance());
            //myStemResult.saveText();


            bigramFrequensy = myStemResult.getBigramFrequensy();
            unigramFrequensy = myStemResult.getUnigramFrequensy();
        } else {

            Reviews reviews = Reviews.readFromFile(Reviews.RU_TRAIN_PATH);
            String data = String.join(" ", reviews.getTexts());

            MyStemOld myStemOldText = new MyStemOld(data, "tt_");

            myStemOldText.saveToFile(MyStemOld.FULL_TEXT);
            myStemOldText = myStemOldText.removeStopWord();
            myStemOldText.saveToFile(MyStemOld.TEXT_WITHOUT_STOPWORDS_txt);
            myStemOldText.saveToFile("-" + File.separator + "text_withoutStopWord.txt");

            myStemOldText.lemmatization();
            myStemOldText.removeStopWordsFromLemmatization();


            bigramFrequensy = myStemOldText.getMyStemResult().getBigramFrequensy();
            unigramFrequensy = myStemOldText.getMyStemResult().getUnigramFrequensy();
        }

        Helper.printUnigram(unigramFrequensy, "result" + File.separator + "unigram_frequency.txt");
        Helper.printUnigram(unigramFrequensy, "-" + File.separator + "_0_unigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "result" + File.separator + "bigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "-" + File.separator + "_0_bigram_frequency.txt");
        settings = new Settings(
                100 / 100.0, 100 / 100.0, 100 / 100.0, 3, 0.65, 2
        );
        DictBase dictBase_base = CSV_DICT.loadFullDict();


        long startTime;
        for (int a = 0; a <= 100; a = a + 3) {
            for (int s = 0; s <= 100; s = s + 3) {
                for (int d = 0; d <= 100; d = d + 3) {
                    startTime = System.currentTimeMillis();
                    settings = new Settings(
                            a / 100.0, s / 100.0, 57 / 100.0, 3, 0.65, 2
                    );
                    DictBase dictBase = dictBase_base.copy();


                    dictBase.removeStopWords();

                    DictBase dictTrain = new DictBase();
                    for (Map.Entry<Bigram, Integer> bigramIntegerEntry : bigramFrequensy.entrySet()) {
                        Bigram key = bigramIntegerEntry.getKey();
                        Edge edge = dictBase.getEdge(dictBase.getVertex(key.getFirst()), dictBase.getVertex(key.getSecond()));

                        if (edge != null)
                            dictTrain.addPair(key.getFirst(), key.getSecond(), edge.getWeight(), edge.getRelationType());
                        else
                            dictTrain.addPair(key.getFirst(), key.getSecond(), Edge.ASS_BASE_WEIGHT, RelationType.ASS);
                    }
                    dictTrain.removeStopWords();


                    DictBase.removeUnusedVertex(dictBase, dictTrain, settings.get_R_());

                    ModificateEdgeInterface modificateEdge =
                            new ModificateEdgeInterfaceImpl(bigramFrequensy, 10, settings.get_R_());
                    modificateEdge.modificate(dictBase);


                    SetVertexWeightInterface setVertexWeightInterface =
                            new SetVertexWeight(unigramFrequensy);
                    setVertexWeightInterface.setVertexWeight(dictBase);

                    CorrectVertexWeightInterface correctVertexWeight = new CorrectVertexWeight(
                            settings.get_R_(), settings.get_GAMMA_(), settings.get_GAMMA_ATTENUATION_RATE_(), true
                    );
                    correctVertexWeight.correctVertexWeight(dictBase);

                    dictBase.calculateWeightOfOutgoingVertex();

                    List<Vertex> allVertex = new ArrayList<>();
                    for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
                        allVertex.add(vertexEdgeMapEntry.getKey());
                    }
                    Collections.sort(allVertex, (o1, o2) -> -Double.compare(o1.getWeight(), o2.getWeight()));
                    StringBuilder pretendents = new StringBuilder();
                    int tmpIndex = 0;
                    DecimalFormat decimalFormat = new DecimalFormat("#0.0000000");
                    for (int i = 0; i < 1000; i++) {
                        Vertex vertex = allVertex.get(i);
                        pretendents
                                .append((tmpIndex++) + ") " + vertex.getWord().getStr() + "\t" + decimalFormat.format(vertex.getWeight()))
                                .append("\n");
                    }
                    Helper.saveToFile(pretendents.toString(), "test" + File.separator +
                            "A" + String.valueOf((int) (settings.get_ASS_WEIGHT_() * 100)) + "_" +
                            "S" + String.valueOf((int) (settings.get_SYN_WEIGHT_() * 100)) + "_" +
                            "D" + String.valueOf((int) (settings.get_DEF_WEIGHT_() * 100)) + ".txt"
                    );


                    List<ClusterHelper> clastering = dictBase.clastering(1, 0.1);
                    String clusterStr = "";
                    tmpIndex = 0;
                    for (ClusterHelper cluster : clastering) {

                        clusterStr += (tmpIndex++) + ")\t" + cluster.getVertex().getWord().getStr() + "\t" + "w=" +
                                cluster.getVertex().getWeight() + "\t" + "wO=" + cluster.getVertex().getWeightOutgoingVertex() +
                                "\t" + "wC=" + cluster.getClusterWeight() + "\n";
                        if (tmpIndex > 1000)
                            break;
                    }
                    Helper.saveToFile(clusterStr, "test" + File.separator +
                            "A" + String.valueOf((int) (settings.get_ASS_WEIGHT_() * 100)) + "_" +
                            "S" + String.valueOf((int) (settings.get_SYN_WEIGHT_() * 100)) + "_" +
                            "D" + String.valueOf((int) (settings.get_DEF_WEIGHT_() * 100)) + "_cluster.txt"
                    );


                    System.out.println("\n====\ntime = " + (System.currentTimeMillis() - startTime) + "\n=====");
                }
            }
        }
    }


    public static void start() throws IOException, DictException, InterruptedException {

        settings = new Settings(
                0.30, 0.12, 0.21, 3, 0.65, 3
//                0.6, 0.3, 0.2, 3, 0.65, 3
        );
        boolean isNew = false;

        Map<Bigram, Integer> bigramFrequensy;
        Map<Unigram, Integer> unigramFrequensy;

        if (isNew) {
            Reviews reviews = Reviews.readFromFile(Reviews.RU_TRAIN_PATH);
            String data = String.join(" ", reviews.getTexts());
            Mystem mystem = new Mystem();
            MyStemResult myStemResult = mystem.analyze(data);

            myStemResult.removeStopWords(StopWords.getInstance());
            //myStemResult.saveText();


            bigramFrequensy = myStemResult.getBigramFrequensy();
            unigramFrequensy = myStemResult.getUnigramFrequensy();
        } else {

            Reviews reviews = Reviews.readFromFile(Reviews.RU_TRAIN_PATH);
            String data = String.join(" ", reviews.getTexts());

            MyStemOld myStemOldText = new MyStemOld(data, "tt_");

            myStemOldText.saveToFile(MyStemOld.FULL_TEXT);
            myStemOldText = myStemOldText.removeStopWord();
            myStemOldText.saveToFile(MyStemOld.TEXT_WITHOUT_STOPWORDS_txt);
            myStemOldText.saveToFile("-" + File.separator + "text_withoutStopWord.txt");

            myStemOldText.lemmatization();
            myStemOldText.removeStopWordsFromLemmatization();


            bigramFrequensy = myStemOldText.getMyStemResult().getBigramFrequensy();
            unigramFrequensy = myStemOldText.getMyStemResult().getUnigramFrequensy();
        }

        Helper.printUnigram(unigramFrequensy, "result" + File.separator + "unigram_frequency.txt");
        Helper.printUnigram(unigramFrequensy, "-" + File.separator + "_0_unigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "result" + File.separator + "bigram_frequency.txt");
        Helper.printBigram(bigramFrequensy, "-" + File.separator + "_0_bigram_frequency.txt");




        DictBase dictBase = CSV_DICT.loadFullDict();
        // dictBase.drawNearVertex("ресторан", 1, "rest.png");
        //dictBase.bidirectional(RelationType.ASS);///////////////////////////////////////////////////////////////////////

        dictBase.removeStopWords();

        DictBase dictTrain = new DictBase();
        for (Map.Entry<Bigram, Integer> bigramIntegerEntry : bigramFrequensy.entrySet()) {
            Bigram key = bigramIntegerEntry.getKey();
            Edge edge = dictBase.getEdge(dictBase.getVertex(key.getFirst()), dictBase.getVertex(key.getSecond()));

            if (edge != null)
                dictTrain.addPair(key.getFirst(), key.getSecond(), edge.getWeight(), edge.getRelationType());
            else
                dictTrain.addPair(key.getFirst(), key.getSecond(), Edge.ASS_BASE_WEIGHT, RelationType.ASS);
        }
        dictTrain.removeStopWords();
        dictTrain.printSortedEdge("-" + File.separator + "_1_dictionary_train.txt");
        dictBase.printSortedEdge("-" + File.separator + "_1_dictionary_base.txt");
        System.out.println("\t\t\tdone");

        calculate_countEdgeByTypes(dictBase);

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

        ModificateEdgeInterface modificateEdge =
                new ModificateEdgeInterfaceImpl(bigramFrequensy, 15, settings.get_R_());
        modificateEdge.modificate(dictBase);
        dictBase.printSortedEdge("-" + File.separator + "_3_dictionary_base after correctEdgeWeight.txt");


        SetVertexWeightInterface setVertexWeightInterface =
                new SetVertexWeight(unigramFrequensy);
        setVertexWeightInterface.setVertexWeight(dictBase);
        dictBase.printSortedVertex("-" + File.separator + "_4_dictionary_base after setVertexWeight.txt");


        //dictBase.saveAs("result" + File.separator + "restaurant.dat");

        CorrectVertexWeightInterface correctVertexWeight = new CorrectVertexWeight(
                settings.get_R_(), settings.get_GAMMA_(), settings.get_GAMMA_ATTENUATION_RATE_(), true
        );
        correctVertexWeight.correctVertexWeight(dictBase);

        //dictBase.drawNearVertex("ресторан", 1, "rest2.png");

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

        List<ClusterHelper> sublist = new ArrayList<>(clastering);

        List<ClusterHelper> forRemoving = new ArrayList<>();
        for (ClusterHelper helper1 : sublist) {
            for (ClusterHelper helper2 : sublist) {
                boolean isSyn = false;
                Edge edge12 = dictBase.checkRelation(helper1.getVertex(), helper2.getVertex());
                Edge edge21 = dictBase.checkRelation(helper2.getVertex(), helper1.getVertex());
                if (edge12 != null && edge12.getRelationType().equals(RelationType.SYN)) {
                    isSyn = true;
                }
                if (edge21 != null && edge21.getRelationType().equals(RelationType.SYN)) {
                    isSyn = true;
                }
                if (isSyn) {
                    System.out.println("====" + helper1.getVertex().getWord().getStr() + " ==== " +
                            helper2.getVertex().getWord().getStr() + "====");
                    if (helper1.getVertex().getWeight() > helper2.getVertex().getWeight()) {
                        forRemoving.add(helper2);
                    } else {
                        forRemoving.add(helper1);
                    }
                }
            }
        }
        //sublist.removeAll(forRemoving);

         tmpIndex = 0;
        System.out.println("рассматриваемые кластера:");
        for (ClusterHelper claster : sublist) {
            if (claster.getVertex().isNoun())
                System.out.println((tmpIndex++) + ") " + claster.getVertex().getWord().getStr() + "\t" + "w=" +
                        claster.getVertex().getWeight() + "\t" + "wO=" + claster.getVertex().getWeightOutgoingVertex() +
                        "\t" + "wC=" + claster.getClusterWeight());
            if (tmpIndex > 40)
                break;
        }
        System.out.println();


        // TODO
        // TODO
        // TODO
        // TODO
        // TODO
        // TODO какой радиус брать инвертированный или нет
        // TODO

//        dictBase.drawNearVertex("ресторан", 0, "restic.png");
        dictBase.distributeVertexIntoClusters(sublist, 15, settings.get_R_() - 1);
//        dictBase.saveAs("result" + File.separator + "restaurant2_new.dat");

        //check(dictBase);

//        Vertex testVertex = dictBase.getVertex("отмечать");
//        Set<Vertex> asdf = dictBase.getInvertMap().keySet();
//        if(asdf.contains(testVertex))
//            System.out.println();

//        DictBase testDict = dictBase.getSubDict(testVertex, 1);
//        testDict.draw("отмечать", Helper.path("result", "test.png"));


        prog22(dictBase);
        System.out.print("");


    }

    private static void calculate_countEdgeByTypes(DictBase dictBase) {
        System.out.println("=======================================================");
        Map<RelationType, Integer> map = new HashMap<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getMap().entrySet()) {
            EdgeMap value = vertexEdgeMapEntry.getValue();
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : value.getEdgeMap().entrySet()) {
                Edge edgeEntryValue = vertexEdgeEntry.getValue();
                RelationType type = edgeEntryValue.getRelationType();
                map.put(
                        type,
                        map.get(type) == null ? 0 : map.get(type) + 1
                );
            }
        }
        for (Map.Entry<RelationType, Integer> relationTypeIntegerEntry : map.entrySet()) {
            Integer count = relationTypeIntegerEntry.getValue();
            RelationType relationType = relationTypeIntegerEntry.getKey();
            System.out.println(relationType.getStr() + "\t" + count);
        }
        System.out.println("=======================================================");
    }

    public static void prog22(DictBase dictBase) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String s = Helper.readFile(Helper.path("data", "semeval", "restaurant", "test", "test.txt"));
        Languagetool languagetool = new Languagetool();
        s = languagetool.getCorrect(s);
        System.out.println(s);

        MyStemOld myStemOld = new MyStemOld(s, UUID.randomUUID().toString());
        myStemOld = myStemOld.removeStopWord();
        myStemOld.lemmatization();
        myStemOld.removeStopWordsFromLemmatization();
        myStemOld.removeTmpFiles();


        List<Sentence> sentencesList2 = myStemOld.getSentencesList2();

        // слово  - соответствующая ему вершина в графе
        Map<String, Vertex> map = dictBase.getStringVertexMap();

        // цикл по предложениям обрабатываемого текста
        for (Sentence sentence : sentencesList2) {
            // список слов предложения приведенных к лексической форме
            List<WordOfSentence> wordOfSentenceList = sentence.getWordOfSentenceList();

            // перебираем слова из предложения, помечаем в какие кластера отностися каждое из слов
            for (WordOfSentence wordOfSentence : wordOfSentenceList) {
                System.out.println("=======\n" + wordOfSentence.getWord());
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
                    System.out.println(clusterIntegerPair.getKey().getVertex().getWord().getStr() + "\t" + clusterIntegerPair.getValue());
                    System.out.print("");
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

            for (Map.Entry<Cluster, Double> clusterDoubleEntry : result.entrySet()) {
                Cluster key = clusterDoubleEntry.getKey();
                Double oldValue = clusterDoubleEntry.getValue();
                double clusterWeight = key.getWeight();
                Double newValue = Math.sqrt(((Math.log(clusterWeight) / Math.log(2))) / clusterWeight) * oldValue;
                result.put(key, newValue);
            }


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

        MyStemOld myStemOld = new MyStemOld(s, UUID.randomUUID().toString());
        myStemOld = myStemOld.removeStopWord();
        myStemOld.lemmatization();
        myStemOld.removeStopWordsFromLemmatization();
        myStemOld.removeTmpFiles();
        List<List<String>> sentencesList = myStemOld.getSentencesList();


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

        List<ClusterHelper> clusterNoun = new ArrayList<>();
        int tmpIndex = 0;
        for (ClusterHelper claster : dictBase.clastering(1, 0.1)) {
            if (claster.getVertex().isNoun())
                clusterNoun.add(claster);
            if (tmpIndex > 40)
                break;
        }

        class CheckHelper {
            int num;
            String str;
            double w;

            public CheckHelper(int num, String str, double w) {
                this.num = num;
                this.str = str;
                this.w = w;
            }
        }

        List<CheckHelper> checkHelperList = new ArrayList<>();
        checkHelperList.add(new CheckHelper(0, "ресторан", 1081.457093968435));
        checkHelperList.add(new CheckHelper(1, "кухня", 413.90067142111377));
        checkHelperList.add(new CheckHelper(2, "интерьер", 403.96756074427947));
        checkHelperList.add(new CheckHelper(8, "действие", 303.04020666838267));
        checkHelperList.add(new CheckHelper(9, "обслуживание", 390.74540059062997));
        checkHelperList.add(new CheckHelper(10, "салат", 256.31315033019007));
        checkHelperList.add(new CheckHelper(11, "порция", 213.34987608559865));
        checkHelperList.add(new CheckHelper(12, "вкус", 203.05622547208844));


        for (CheckHelper checkHelper : checkHelperList) {
            if (!checkHelper.str.equals(clusterNoun.get(checkHelper.num).getVertex().getWord().getStr()) ||
                    Math.abs(clusterNoun.get(checkHelper.num).getVertex().getWeight() - checkHelper.w) > 0.00001) {
                throw new IOException("ЕЕЕЕРРРРРРОООООРРРРР");
            }
        }
    }
}
