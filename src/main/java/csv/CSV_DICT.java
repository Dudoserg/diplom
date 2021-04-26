package csv;

import com.opencsv.bean.CsvToBeanBuilder;
import dict.*;
import mystem.MyStem;
import mystem.MyStemItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class CSV_DICT {
    public static DictBase loadFullDict() throws DictException, FileNotFoundException {
        System.out.print("loadFullDict...");

        String words_fileName = "data" + File.separator + "words.csv";
        String connections_fileName = "data" + File.separator + "connections.csv";

        List<CSV_words> words = new CsvToBeanBuilder(new FileReader(words_fileName)).withSeparator(';')
                .withType(CSV_words.class)
                .build()
                .parse();

        Map<Integer, CSV_words> words_map = words.stream()
                .collect(Collectors.toMap(CSV_words::getId, csv_words -> csv_words));

        List<CSV_connections> connections = new CsvToBeanBuilder(new FileReader(connections_fileName)).withSeparator(';')
                .withType(CSV_connections.class)
                .build()
                .parse();

        DictBase dict = new DictBase();
        for (CSV_connections con : connections) {
            CSV_words wordFrom = words_map.get(con.getWordFrom());
            CSV_words wordTo = words_map.get(con.getWordTo());

            double weight = 0.0;
            RelationType relationType = null;

            if(con.getDefWeight() > 0){
                weight = con.getDefWeight();
                weight = 0.2;
                relationType = RelationType.DEF;
            }else if(con.getAssWeight() > 0){
                weight = con.getAssWeight();
                weight = 0.6;
                relationType = RelationType.ASS;
            }else if(con.getSynWeight() > 0){
                weight = con.getSynWeight();
                weight = 0.3;
                relationType = RelationType.SYN;
            }else {
                throw new DictException("weight of edge equals 0.0 [id=" + con.getId() + "]");
            }
            if(wordFrom.getSpelling().matches("[a-zA-Z]+") || wordTo.getSpelling().matches("[a-zA-Z]+")){
                continue;
            }
            dict.addPair(wordFrom.getSpelling(), wordTo.getSpelling(), weight, relationType);

            Word.getWord(wordFrom.getSpelling()).setWordType(WordType.create(wordFrom.getPosId()));
            Word.getWord(wordTo.getSpelling()).setWordType(WordType.create(wordTo.getPosId()));
        }
        System.out.println("\t\t\tdone");



        ////// Вычисляем часть речи каждого из слов
        List<String> listOfWords = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dict.getInvertMap().entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            listOfWords.add(key.getWord().getStr());
        }

        MyStem myStem = new MyStem(listOfWords, "dd_");
        myStem.lemmatization();


        for (MyStemItem myStemItem : myStem.getMyStemResult().getItemList()) {
            myStemItem.calcPartOfSpeech();
            PartOfSpeech partOfSpeech = myStemItem.getPartOfSpeech();
        }

        return dict;

    }
}
