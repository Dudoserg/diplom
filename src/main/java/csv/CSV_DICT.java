package csv;

import Main.Main;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import dict.*;
import mystem.MyStem;
import mystem.MyStemItem;
import mystem.MyStemResult;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CSV_DICT {
    public static DictBase loadFullDict() throws DictException, IOException {
        System.out.print("loadFullDict...");

        String words_fileName = "data" + File.separator + "words2.csv";
        String connections_fileName = "data" + File.separator + "connections.csv";

        List<CSV_words> words = new CsvToBeanBuilder(new FileReader(words_fileName)).withSeparator(';')
                .withType(CSV_words.class)
                .build()
                .parse();

        create_список_слов_с_типомДанных(words);

        Map<Integer, CSV_words> words_map = words.stream()
                .collect(Collectors.toMap(CSV_words::getId, csv_words -> csv_words));

        List<CSV_connections> connections = new CsvToBeanBuilder(new FileReader(connections_fileName)).withSeparator(';')
                .withType(CSV_connections.class)
                .build()
                .parse();

        DictBase dict = new DictBase();
        CSV_DICT_FULL csv_dict_full = new CSV_DICT_FULL();
        for (CSV_connections con : connections) {
            CSV_words wordFrom = words_map.get(con.getWordFrom());
            CSV_words wordTo = words_map.get(con.getWordTo());

            double weight = 0.0;
            RelationType relationType = null;

            if (con.getDefWeight() > 0) {
                weight = con.getDefWeight();
                weight = Main.settings.get_DEF_WEIGHT_();
                relationType = RelationType.DEF;
            } else if (con.getAssWeight() > 0) {
                weight = con.getAssWeight();
                weight = Main.settings.get_ASS_WEIGHT_();
                relationType = RelationType.ASS;
            } else if (con.getSynWeight() > 0) {
                weight = con.getSynWeight();
                weight = Main.settings.get_SYN_WEIGHT_();
                relationType = RelationType.SYN;
            } else {
                throw new DictException("weight of edge equals 0.0 [id=" + con.getId() + "]");
            }
            if (wordFrom.getSpelling().matches("[a-zA-Z]+") || wordTo.getSpelling().matches("[a-zA-Z]+")) {
                continue;
            }
            Vertex fromVertex = Vertex.getVertex(wordFrom.getSpelling());
            fromVertex.getWord().setPartOfSpeech(PartOfSpeech.getPart(wordFrom.getPartOfSpeech()));

            Vertex toVertex = Vertex.getVertex(wordTo.getSpelling());
            toVertex.getWord().setPartOfSpeech(PartOfSpeech.getPart(wordTo.getPartOfSpeech()));


            dict.addPair(wordFrom.getSpelling(), wordTo.getSpelling(), weight, relationType);

            Word.getWord(wordFrom.getSpelling()).setWordType(WordType.create(wordFrom.getPosId()));
            Word.getWord(wordTo.getSpelling()).setWordType(WordType.create(wordTo.getPosId()));


            csv_dict_full.addRow(wordFrom, wordFrom, relationType);
        }

        System.out.println("\t\t\tdone");


        ////// ГЛОБАЛЬНО Вычисляем часть речи каждого из слов словаря

        //dict = calculatePartOfSpeech(dict);

        return dict;

    }

    private static DictBase calculatePartOfSpeech(DictBase dict) throws IOException {
        List<String> listOfWords = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dict.getInvertMap().entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            listOfWords.add(key.getWord().getStr());
        }

        MyStem myStem = new MyStem(listOfWords, "dd_");
        myStem.saveToFile(MyStem.TEXT_WITHOUT_STOPWORDS_txt);
        myStem.lemmatization();


        for (MyStemItem myStemItem : myStem.getMyStemResult().getItemList()) {
            myStemItem.calcPartOfSpeech();
            PartOfSpeech partOfSpeech = myStemItem.getPartOfSpeech();
            Vertex vertex = Vertex.getVertex(myStemItem.getText());
            vertex.getWord().setPartOfSpeech(partOfSpeech);
            System.out.print("");
        }
        return dict;

    }

    private static void create_список_слов_с_типомДанных(List<CSV_words> words) throws IOException {
        // извлекаем все слова из словаря
        List<String> unique = new ArrayList<>();
        for (CSV_words word : words) {
            String spelling = word.getSpelling();
            unique.add(spelling);
        }
        String collect = unique.stream().collect(Collectors.joining(" . "));

        // определяем части речи данных слов
        MyStem myStem = new MyStem(collect, "qwe");
        myStem.saveToFile(MyStem.TEXT_WITHOUT_STOPWORDS_txt);
        myStem.lemmatization();
        MyStemResult myStemResult = myStem.getMyStemResult();

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
            if(partOfSpeech == null){
                System.out.print("");
            }else{
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
