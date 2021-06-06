package Main;

import com.oracle.truffle.api.ArrayUtils;
import dict.Cluster;
import dict.DictBase;
import dict.Vertex;
import javafx.util.Pair;
import mystem.MyStemOld;
import org.checkerframework.checker.units.qual.C;
import prog2.Sentence;
import prog2.WordOfSentence;
import utils.Helper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main2 {
    public static void main(String[] args) {
        Long s = System.currentTimeMillis();
        System.out.print("read dictionary...\t\t");
        DictBase dictBase = DictBase.loadFromFiles("");
        System.out.println("done for " + (System.currentTimeMillis() - s) + " ms.");
        try {
            prog22(dictBase);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void prog22(DictBase dictBase) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        String s = Helper.readFile(Helper.path("data", "semeval", "restaurant", "test", "test.txt"));

        //Languagetool languagetool = new Languagetool();
        //s = languagetool.getCorrect(s);
        System.out.println(s);

        MyStemOld myStemOld = new MyStemOld(s, UUID.randomUUID().toString());
        myStemOld = myStemOld.removeStopWord();
        myStemOld.lemmatization();
        myStemOld.removeStopWordsFromLemmatization();
        myStemOld.removeTmpFiles();


        List<Sentence> sentencesList2 = myStemOld.getSentencesList2();

        // слово  - соответствующая ему вершина в графе
        Map<String, Vertex> map = dictBase.getStringVertexMap();


        AnalyzerRezult analyzerRezult = new AnalyzerRezult();

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
            }
            System.out.println();
            sentence.setResult(result);
            analyzerRezult.addSentence(sentence);
        }

        List<Double> values = new ArrayList<>();
        for (Sentence sentence : analyzerRezult.getSentenceList()) {
            int i = 0;
            for (Map.Entry<Cluster, Double> clusterDoubleEntry : sentence.getResult().entrySet()) {
                values.add(clusterDoubleEntry.getValue());
                i++;
                if (i >= 5)
                    break;
                ;
            }
        }
        Collections.sort(values);
        double maxValue = values.stream().mapToDouble(value -> value.doubleValue()).max().orElse(0.0);

        values = values.stream()
                .filter(aDouble -> Math.abs(aDouble) > maxValue * 0.06 )
                .collect(Collectors.toList());
        double median = CustomMath.FindMedian(values.stream().mapToDouble(Double::doubleValue).toArray());
        double geometricAverage = CustomMath.FindGeometricAverage(values.stream().mapToDouble(Double::doubleValue).toArray());

        for (Sentence sentence : analyzerRezult.getSentenceList()) {
        }

        System.out.println("prog 22 time = " + (System.currentTimeMillis() - startTime) + " ms.");

    }

}
