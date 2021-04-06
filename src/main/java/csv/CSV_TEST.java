package csv;

import com.opencsv.bean.CsvToBeanBuilder;
import dict.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSV_TEST {
    public static void main(String[] args) throws FileNotFoundException, DictException {
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
            dict.addPair(wordFrom.getSpelling(), wordTo.getSpelling(), new Edge(con.getDefWeight(),con.getSynWeight(), con.getAssWeight() ));

            Word.getStr(wordFrom.getSpelling()).setWordType(WordType.create(wordFrom.getPosId()));
            Word.getStr(wordTo.getSpelling()).setWordType(WordType.create(wordTo.getPosId()));
        }

        System.out.println();
//        for (CSV_words word : words) {
//            System.out.println(word.getId() + "\t" + word.getSpelling() + "\t" + word.getPosId());
//        }
    }
}