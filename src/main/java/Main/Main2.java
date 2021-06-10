package Main;

import Main.Analyzer.AnalyzerRezult;
import Main.Analyzer.CustomMath;
import Main.Analyzer.Senti.SentiLoader;
import Main.Analyzer.SentiAnalyze;
import Main.Analyzer.SentiResult.*;
import SpellerChecker.Languagetool;
import SpellerChecker.SpellCheckingInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import dict.Cluster;
import dict.DictBase;
import dict.DictException;
import dict.Vertex;
import mystem.MyStemOld;
import mystem.StopWords;
import prog2.Sentence;
import prog2.WordOfSentence;
import settings.Settings_analyzer;
import utils.Analyzer_ARGS;
import utils.Helper;
import utils.Pair;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main2 {
    public static void main(String[] args) throws DictException, IOException, IllegalAccessException {

        try {
            Analyzer_ARGS analyzer_args = new Analyzer_ARGS(args);

            System.out.println(analyzer_args.getSetting());
            System.out.println("!!!?!?!??");
            System.in.read();

            Settings_analyzer instance = Settings_analyzer.load(
                    analyzer_args.getSetting()
            );


            Long s = System.currentTimeMillis();
            System.out.print("read dictionary...\t\t");
            DictBase dictBase = DictBase.loadFromFiles(Settings_analyzer.getInstance().getDomainPath());
            System.out.println("done for " + (System.currentTimeMillis() - s) + " ms.");


            ///String name = "ресторан";
            //Vertex ресторан = dictBase.getVertex(name);
            //{
            // DictBase subdict = dictBase.getFullSubDict(ресторан, 0);
            // DictBase.graphviz_draw(DictBase.graphviz_getGraphViz(subdict, name), "limonad.png");
            //}


            try {
                if (analyzer_args.getOtziv() != null)
                    prog22(dictBase, Helper.path(analyzer_args.getOtziv()));
                else
                    prog22(dictBase, Helper.path("bin", "test.txt"));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.in.read();
        }

    }

    public static void prog22(DictBase dictBase, String otzivPath) throws IOException, InterruptedException, IllegalAccessException {
        String s = Helper.readFile(otzivPath);

        SpellCheckingInterface spellCheker = new Languagetool();
        s = spellCheker.getCorrect(s);
        System.out.println(s);

        MyStemOld myStemOld = new MyStemOld(s, UUID.randomUUID().toString());

        StopWords stopWords = StopWords.getInstance();
        stopWords.ignore("не");

        myStemOld = myStemOld.removeStopWord(stopWords);
        myStemOld.lemmatization();
        long startTime = System.currentTimeMillis();

        myStemOld.removeStopWordsFromLemmatization();
        myStemOld.removeTmpFiles();


        List<Sentence> sentencesList2 = myStemOld.getSentencesList2();

        // слово  - соответствующая ему вершина в графе
        Map<String, Vertex> map = dictBase.getStringVertexMap();


        AnalyzerRezult analyzerRezult = new AnalyzerRezult();

        SentiResult sentiResult = new SentiResult();
        // цикл по предложениям обрабатываемого текста
        for (Sentence sentence : sentencesList2) {
            SentiResultSentence sentiResultSentence = new SentiResultSentence();
            sentiResultSentence.setOriginalStr(sentence.getProcessedText());
            sentiResult.getSentiResultSentenceList().add(sentiResultSentence);
            sentiResultSentence.setLemmatizationStr(
                    sentence.getWordOfSentenceList().stream()
                            .map(WordOfSentence::getWord)
                            .collect(Collectors.joining(" "))
            );
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
                WordAndClusters wordAndClusters = new WordAndClusters(wordOfSentence.getWord());
                sentiResultSentence.getWordAndClustersList().add(wordAndClusters);

                for (Pair<Cluster, Integer> clusterIntegerPair : vertex.getShortest()) {
                    wordOfSentence.addCluster(
                            clusterIntegerPair.getFirst(),
                            clusterIntegerPair.getSecond()
                    );
                    System.out.println(clusterIntegerPair.getFirst().getVertex().getWord().getStr() + "\t" + clusterIntegerPair.getSecond());
                    System.out.print("");

                    wordAndClusters.getClusterAndDistanceList().add(
                            new ClusterAndDistance(clusterIntegerPair.getFirst().getVertex().getWord().getStr(), clusterIntegerPair.getSecond())
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

            Map<Cluster, Double> result = dictBase.getClusterList().stream()
                    .collect(Collectors.toMap(
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
                sentiResultSentence.getClustersAndWeightList().add(
                        new Pair<>(elem.getKey().getVertex().getWord().getStr(), elem.getValue())
                );
            }
            System.out.println();

            sentence.setResultMap(result);
            analyzerRezult.addSentence(sentence);
        }

        List<Double> values = new ArrayList<>();
        for (Sentence sentence : analyzerRezult.getSentenceList()) {
            for (int i = 0; i < 1; i++) {
                Pair<Cluster, Double> clusterDoublePair = sentence.getResult().get(i);
                Cluster first = clusterDoublePair.getFirst();
                Double second = clusterDoublePair.getSecond();
                values.add(second);
            }
        }
        Collections.sort(values);
        double maxValue = values.stream().mapToDouble(value -> value.doubleValue()).max().orElse(0.0);

//        values = values.stream()
//                .filter(aDouble -> Math.abs(aDouble) > maxValue * 0.06)
//                .collect(Collectors.toList());
        double median = CustomMath.FindMedian(values.stream().mapToDouble(Double::doubleValue).toArray());
        median *= 0.6;
        double geometricAverage = CustomMath.FindGeometricAverage(values.stream().mapToDouble(Double::doubleValue).toArray());

        SentiAnalyze sentiAnalyze = new SentiAnalyze(new SentiLoader());
        Map<String, List<Double>> clusterAndValues = new HashMap<>();
        int counter = 0;
        for (Sentence sentence : analyzerRezult.getSentenceList()) {

            Pair<Cluster, Double> pair = sentence.getResult().get(0);
            Double value = pair.getSecond();
            if (value < median)
                continue;
            Double analyzeValue = sentiAnalyze.analyze(sentence);
            sentence.setSentiAnalyzeResult(analyzeValue);

            // устанавливаем оценку предложения в результирующий файл
            sentiResult.getSentiResultSentenceList().get(counter++).setSentenceSentiMark(analyzeValue);

            String clusterStr = pair.getFirst().getVertex().getWord().getStr();
            List<Double> doubles = clusterAndValues.get(clusterStr);
            if (doubles == null) {
                doubles = new ArrayList<>();
            }
            doubles.add(analyzeValue);
            clusterAndValues.put(clusterStr, doubles);
        }

        List<Pair<String, Double>> collect = clusterAndValues.entrySet().stream()
                .map(
                        e -> {
                            double avg = e.getValue().stream().mapToDouble(value -> value).average().orElse(0.0);
                            return new Pair<String, Double>(e.getKey(), avg);
                        }
                )
                .sorted((o1, o2) -> -Double.compare(o1.getSecond(), o2.getSecond()))
                .filter(e -> Math.abs(e.getSecond()) > 0.5)
                .collect(Collectors.toList());

        System.out.println("\n\n");
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        for (Pair<String, Double> e : collect) {
            System.out.println(e.getFirst() + "\t=\t" + decimalFormat.format(e.getSecond()));
            sentiResult.getResult().add(new WordAndValue(e.getFirst(), decimalFormat.format(e.getSecond())));
        }
        System.out.println("\n\n");

        System.out.println("prog 22 time = " + (System.currentTimeMillis() - startTime) + " ms.");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(sentiResult);
        String resultPath = Helper.path(Settings_analyzer.getInstance().getResultPath());
        Helper.saveToFile(json, resultPath);

    }

}
