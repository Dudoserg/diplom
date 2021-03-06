package csv;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import constant.CONST;
import dict.*;

import mystem.MyStemOld;
import mystem.MyStemItem;
import mystem.MyStemResult;
import settings.Settings_constructor;
import utils.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DictionaryLoader {


    public static DictBase loadFullDict_old() throws DictException, IOException, IllegalAccessException {
        System.out.print("loadFullDict...");

        String words_fileName = "data" + File.separator + "words2.csv";
        String connections_fileName = "data" + File.separator + "connections.csv";

        List<CSV_words> words = new CsvToBeanBuilder(new FileReader(words_fileName)).withSeparator(';')
                .withType(CSV_words.class)
                .build()
                .parse();


        Map<Integer, CSV_words> words_map = words.stream()
                .collect(Collectors.toMap(CSV_words::getId, csv_words -> csv_words));

        List<CSV_connections_old> connections = new CsvToBeanBuilder(new FileReader(connections_fileName)).withSeparator(';')
                .withType(CSV_connections_old.class)
                .build()
                .parse();


        create_Words2_список_слов_с_типомДанных(words);
        createConnections(connections, words_map);

        DictBase dict = new DictBase();
        CSV_DICT_FULL csv_dict_full = new CSV_DICT_FULL();
        for (CSV_connections_old con : connections) {
            CSV_words wordFrom = words_map.get(con.getWordFrom());
            CSV_words wordTo = words_map.get(con.getWordTo());

            double weight = 0.0;
            RelationType relationType = null;
            // TODO ПОПРАВИТЬ, ВЫБРАТЬ МАКСИМАЛЬНЫЙ ВЕС, А НЕ ПРОСТО больше 0
            if (con.getDefWeight() > 0) {
                weight = con.getDefWeight();
                weight = Settings_constructor.getInstance().get_DEF_WEIGHT_();
                relationType = RelationType.DEF;
            } else if (con.getAssWeight() > 0) {
                weight = con.getAssWeight();
                weight = Settings_constructor.getInstance().get_ASS_WEIGHT_();
                relationType = RelationType.ASS;
            } else if (con.getSynWeight() > 0) {
                weight = con.getSynWeight();
                weight = Settings_constructor.getInstance().get_SYN_WEIGHT_();
                relationType = RelationType.SYN;
            } else {
                throw new DictException("weight of edge equals 0.0 [id=" + con.getId() + "]");
            }
            if (wordFrom.getSpelling().matches("[a-zA-Z]+") || wordTo.getSpelling().matches("[a-zA-Z]+")) {
                continue;
            }
            Vertex fromVertex = dict.getVertex(wordFrom.getSpelling());
            fromVertex.getWord().setPartOfSpeech(PartOfSpeech.getPart(wordFrom.getPartOfSpeech()));

            Vertex toVertex = dict.getVertex(wordTo.getSpelling());
            toVertex.getWord().setPartOfSpeech(PartOfSpeech.getPart(wordTo.getPartOfSpeech()));


            dict.addPair(wordFrom.getSpelling(), wordTo.getSpelling(), weight, relationType);

            Word.getWord(wordFrom.getSpelling()).setWordType(WordType.create(wordFrom.getPosId()));
            Word.getWord(wordTo.getSpelling()).setWordType(WordType.create(wordTo.getPosId()));


            csv_dict_full.addRow(wordFrom, wordFrom, relationType);
        }

        System.out.println("\t\t\tdone");


        ////// ГЛОБАЛЬНО Вычисляем часть речи каждого из слов словаря

        dict = calculatePartOfSpeech(dict);

        return dict;

    }


    public static DictBase loadFullDict() throws IOException, DictException, IllegalAccessException {
        System.out.print("read Dictionary from file...\t\t");
        Long t = System.currentTimeMillis();


//        String connections_fileName = "data" + File.separator + "connections2.csv";
        String connections_fileName = CONST.DICTIONARY_PATH;

        List<CSV_CONNECTIONS> connections = new CsvToBeanBuilder(new FileReader(connections_fileName))
                .withSeparator(';')
                .withType(CSV_CONNECTIONS.class)
                .build()
                .parse();

        DictBase dict = new DictBase();
        int j = 2;
        for (CSV_CONNECTIONS con : connections) {

            Vertex vertexFrom = dict.getVertex(con.getWordFrom(), con.getPartOfSpeechFrom());

            Vertex vertexTo = dict.getVertex(con.getWordTo(), con.getPartOfSpeechTo());
            if (j == 4907)
                System.out.print("");
            if ("ресторан".equals(vertexFrom.getWord().getStr()))
                System.out.print("");
            if ("ресторан".equals(vertexTo.getWord().getStr()))
                System.out.print("");

            dict.addPair(
                    con.getWordFrom(),
                    con.getWordTo(),
                    Settings_constructor.getInstance().getWeight(con.getRelationType()),
                    con.getRelationType()
            );
            j++;
        }

        System.out.println("done for " + (System.currentTimeMillis() - t) + " ms.");
        return dict;
    }


    public static DictBase loadFullDictTestThreads() throws FileNotFoundException, DictException, InterruptedException {

        List<Pair<DictBase, String>> names = new ArrayList<>();
        names.add(new Pair<>(new DictBase(), "data" + File.separator + "test1.csv"));
        names.add(new Pair<>(new DictBase(), "data" + File.separator + "test2.csv"));
        names.add(new Pair<>(new DictBase(), "data" + File.separator + "test3.csv"));
        names.add(new Pair<>(new DictBase(), "data" + File.separator + "test4.csv"));


        List<Thread> threadList = new ArrayList<>();

        for (Pair<DictBase, String> row : names) {
            threadList.add(new Thread(() -> {
                List<CSV_CONNECTIONS> connections = null;
                try {
                    connections = new CsvToBeanBuilder(new FileReader(row.getSecond()))
                            .withSeparator(';')
                            .withType(CSV_CONNECTIONS.class)
                            .build()
                            .parse();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                DictBase dict = row.getFirst();
                int j = 2;
                for (CSV_CONNECTIONS con : connections) {

                    Vertex vertexFrom = dict.getVertex(con.getWordFrom(), con.getPartOfSpeechFrom());

                    Vertex vertexTo = dict.getVertex(con.getWordTo(), con.getPartOfSpeechTo());
                    if (j == 4907)
                        System.out.print("");
                    if ("ресторан".equals(vertexFrom.getWord().getStr()))
                        System.out.print("");
                    if ("ресторан".equals(vertexTo.getWord().getStr()))
                        System.out.print("");

                    try {
                        dict.addPair(
                                con.getWordFrom(),
                                con.getWordTo(),
                                Settings_constructor.getInstance().getWeight(con.getRelationType()),
                                con.getRelationType()
                        );
                    } catch (DictException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    j++;
                }
            }));
        }


        for (Thread thread : threadList) {
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }

        System.out.print("");
        return null;
    }


    private static DictBase calculatePartOfSpeech(DictBase dict) throws IOException {
        List<String> listOfWords = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dict.getInvertMap().entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            listOfWords.add(key.getWord().getStr());
        }

        MyStemOld myStemOld = new MyStemOld(listOfWords, "dd_");
        myStemOld.saveToFile(MyStemOld.TEXT_WITHOUT_STOPWORDS_txt);
        try {
            myStemOld.lemmatization();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (MyStemItem myStemItem : myStemOld.getMyStemResult().getItemList()) {
            myStemItem.calcPartOfSpeech();
            PartOfSpeech partOfSpeech = myStemItem.getPartOfSpeech();
            Vertex vertex = dict.getVertex(myStemItem.getText());
            vertex.getWord().setPartOfSpeech(partOfSpeech);
            System.out.print("");
        }
        return dict;

    }


    private static void createConnections(List<CSV_connections_old> connections, Map<Integer, CSV_words> words_map) throws DictException {
        List<CSV_CONNECTIONS> result = new ArrayList<>();
        for (CSV_connections_old connection : connections) {

            CSV_words wordFrom = words_map.get(connection.getWordFrom());
            CSV_words wordTo = words_map.get(connection.getWordTo());


            RelationType relationType = null;
            if (connection.getDefWeight() > 0) {
                relationType = RelationType.DEF;
            } else if (connection.getAssWeight() > 0) {
                relationType = RelationType.ASS;
            } else if (connection.getSynWeight() > 0) {
                relationType = RelationType.SYN;
            } else {
                throw new DictException("weight of edge equals 0.0 [id=" + connection.getId() + "]");
            }

            CSV_CONNECTIONS csv_connections = CSV_CONNECTIONS.create(
                    connection.getId(),
                    wordFrom.getSpelling().toLowerCase(),
                    PartOfSpeech.getPart(wordFrom.getPartOfSpeech()),
                    wordTo.getSpelling().toLowerCase(),
                    PartOfSpeech.getPart(wordTo.getPartOfSpeech()),
                    relationType
            );
            result.add(csv_connections);
        }

        try (
                Writer writer = Files.newBufferedWriter(Paths.get("data" + File.separator + "connections2.csv"));
        ) {
            StatefulBeanToCsv<CSV_CONNECTIONS> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withSeparator(';')
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();
            beanToCsv.write(result);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            e.printStackTrace();
        }

    }


    private static void create_Words2_список_слов_с_типомДанных(List<CSV_words> words) throws IOException {
        // извлекаем все слова из словаря
        List<String> unique = new ArrayList<>();
        for (CSV_words word : words) {
            String spelling = word.getSpelling();
            unique.add(spelling);
        }
        String collect = unique.stream().collect(Collectors.joining(" . "));

        // определяем части речи данных слов
        MyStemOld myStemOld = new MyStemOld(collect, "qwe");
        myStemOld.saveToFile(MyStemOld.TEXT_WITHOUT_STOPWORDS_txt);
        try {
            myStemOld.lemmatization();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MyStemResult myStemResult = myStemOld.getMyStemResult();

        // создаем хешмап с ключом - слово , значение - часть речи
        HashMap<String, PartOfSpeech> map = new HashMap<>();

        for (MyStemItem myStemItem : myStemResult.getItemList()) {
            String text = myStemItem.getText();
            PartOfSpeech partOfSpeech = myStemItem.getPartOfSpeech();
            map.put(text, partOfSpeech);
        }
        String fileName = "words2.csv";

        // создаем новый словарь, добавив в него часть речи
        for (CSV_words word : words) {
            PartOfSpeech partOfSpeech = map.get(word.getSpelling().toLowerCase());
            if (partOfSpeech == null) {
                System.out.print("");
            } else {
                word.setPartOfSpeech(partOfSpeech.getStr());
            }
        }


        try (
                Writer writer = Files.newBufferedWriter(Paths.get("data" + File.separator + fileName));
        ) {
            StatefulBeanToCsv<CSV_words> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withSeparator(';')
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build();
            beanToCsv.write(words);
        } catch (CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        } catch (CsvDataTypeMismatchException e) {
            e.printStackTrace();
        }

    }


}
