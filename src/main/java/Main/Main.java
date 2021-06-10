package Main;

import Ngrams.Ngrams;
import Ngrams.NgramsInt;
import csv.DictionaryLoader;
import data.TrainLoader;
import dict.*;
import dict.CorrectVertexWeight.CorrectVertexWeight;
import dict.CorrectVertexWeight.CorrectVertexWeightInterface;
import dict.Edge.Edge;
import dict.ModificateEdge.ModificateEdgeInterface;
import dict.ModificateEdge.ModificateEdgeByBigramm;
import dict.SetVertexWeight.SetVertexWeight;
import dict.SetVertexWeight.SetVertexWeightInterface;

import mystem.*;
import settings.Settings_constructor;
import utils.*;

import java.io.IOException;
import java.util.*;

public class Main {
    static Integer start;
    static Integer finish;
    //public static Settings settings;

    public static void main(String[] args) throws Exception {

        Constructor_ARGS constructorARGS = new Constructor_ARGS(args);

        System.out.println(constructorARGS.getSetting());
        System.out.println("ПОЕХАЛИ?");
        System.in.read();

        Settings_constructor instance = Settings_constructor.load(
                constructorARGS.getSetting()
        );

//        instance.set_ASS_WEIGHT_(0.15);
//        instance.set_SYN_WEIGHT_(0.1);
//        instance.set_DEF_WEIGHT_(0.3);
//        instance.set_GAMMA_(0.65);
//        instance.set_R_(3);
//        instance.set_GAMMA_ATTENUATION_RATE_(3);
//        instance.setCountThreads(Runtime.getRuntime().availableProcessors());

        DictBase dictBase = start();

        Long s = System.currentTimeMillis();
        dictBase.saveToFile();
        System.out.println(System.currentTimeMillis() - s);

        System.out.print("------------------------------------------------------------------\n");
        System.in.read();
        System.in.read();
    }


    public static DictBase start() throws IOException, DictException, InterruptedException, IllegalAccessException {

        boolean isNew = true;

        Map<Bigram, Integer> bigramFrequensy;
        Map<Unigram, Integer> unigramFrequensy;

        if (isNew) {
            TrainLoader trainLoader = new TrainLoader();

            List<String> load = trainLoader.load(Settings_constructor.getInstance().getTrainPath());

            String data = String.join(" ", load);

            Mystem mystem = new Mystem();

            MyStemResult myStemResult = mystem.analyze(data);
            myStemResult.removeStopWords(StopWords.getInstance());

            NgramsInt ngrams = new Ngrams();
            bigramFrequensy = ngrams.getBigramFrequensy(myStemResult.getItemList());
            unigramFrequensy = ngrams.getUnigramFrequensy(myStemResult.getItemList());
        } else {
            TrainLoader trainLoader = new TrainLoader();
            List<String> load = trainLoader.load(Settings_constructor.getInstance().getTrainPath());

            String data = String.join(" ", load);

            MyStemOld myStemOldText = new MyStemOld(data, "tt_");

            myStemOldText.saveToFile(MyStemOld.FULL_TEXT);
            myStemOldText = myStemOldText.removeStopWord(StopWords.getInstance());
            myStemOldText.saveToFile(MyStemOld.TEXT_WITHOUT_STOPWORDS_txt);
            //myStemOldText.saveToFile("-" + File.separator + "text_withoutStopWord.txt");

            myStemOldText.lemmatization();
            myStemOldText.removeStopWordsFromLemmatization();

            NgramsInt ngrams = new Ngrams();
            bigramFrequensy = ngrams.getBigramFrequensy(myStemOldText.getMyStemResult().getItemList());
            unigramFrequensy = ngrams.getUnigramFrequensy(myStemOldText.getMyStemResult().getItemList());
        }

        //Helper.printUnigram(unigramFrequensy, "result" + File.separator + "unigram_frequency.txt");
        //Helper.printUnigram(unigramFrequensy, "-" + File.separator + "_0_unigram_frequency.txt");
        //Helper.printBigram(bigramFrequensy, "result" + File.separator + "bigram_frequency.txt");
        //Helper.printBigram(bigramFrequensy, "-" + File.separator + "_0_bigram_frequency.txt");


        DictBase dictBase = DictionaryLoader.loadFullDict();
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

        //dictTrain.printSortedEdge("-" + File.separator + "_1_dictionary_train.txt");
        //dictBase.printSortedEdge("-" + File.separator + "_1_dictionary_base.txt");
        //System.out.println("\t\t\tdone");

        calculate_countEdgeByTypes(dictBase);


        DictBase.removeUnusedVertex(dictBase, dictTrain, Settings_constructor.getInstance().get_R_());
        //dictBase.printSortedEdge("-" + File.separator + "_2_dictionary_base after removeUnusedVertex.txt");



        ModificateEdgeInterface modificateEdge =
                new ModificateEdgeByBigramm(bigramFrequensy, 15, Settings_constructor.getInstance().get_R_());
        modificateEdge.modificate(dictBase);
        //dictBase.printSortedEdge("-" + File.separator + "_3_dictionary_base after correctEdgeWeight.txt");

        //////////////////
        SetVertexWeightInterface setVertexWeightInterface =
                new SetVertexWeight(unigramFrequensy);
        setVertexWeightInterface.setVertexWeight(dictBase);
        //dictBase.printSortedVertex("-" + File.separator + "_4_dictionary_base after setVertexWeight.txt");


        //////////////////
        long l = System.currentTimeMillis();
        CorrectVertexWeightInterface correctVertexWeight = new CorrectVertexWeight(
                Settings_constructor.getInstance().get_R_(),
                Settings_constructor.getInstance().get_GAMMA_(),
                Settings_constructor.getInstance().get_GAMMA_ATTENUATION_RATE_(),
                Settings_constructor.getInstance().getCountThreads()
        );
        correctVertexWeight.correctVertexWeight(dictBase);
        System.out.println("THTIME = " + (System.currentTimeMillis() - l));

        //////////////////
        //dictBase.printSortedVertex("-" + File.separator + "_5_dictionary_base after correctVertexWeight(r=" +
        //        Settings.getInstance().get_R_() + ",gamma=" + Settings.getInstance().get_GAMMA_() + " 3 затухание).txt");
        //dictBase.printSortedVertex("-" + File.separator + "_5_dictionary_base NOUN after correctVertexWeight(r=" +
        //        Settings.getInstance().get_R_() + ",gamma=" + Settings.getInstance().get_GAMMA_() + " 3 затухание).txt", PartOfSpeech.NOUN, 100);

        //////////////////
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

        /// Удаляем похожие кластера
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


        // TODO какой радиус брать инвертированный или нет
        dictBase.distributeVertexIntoClusters(sublist, 15, Settings_constructor.getInstance().get_R_() - 1);


        return dictBase;
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


}