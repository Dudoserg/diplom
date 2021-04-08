package csv;

import com.opencsv.bean.CsvToBeanBuilder;
import dict.*;
import utils.Helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSV_TEST {
    public static void main(String[] args) throws IOException, DictException {
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
        //dict.addPair("соответствующий", "значение", new Edge(0.0,0.0,0.0));

        //List<List<Vertex>> ways = dict.findWays(new Vertex("значение"), new Vertex("соответствующий"), 5);
//        for (List<Vertex> way : ways) {
//            System.out.print(way.stream().map(vertex -> vertex.getWord().getStr()).collect(Collectors.joining("  ")));
//            System.out.println("\t\t" + way.size());
//        }
        System.out.print("\n\n\n");
        List<List<Vertex>> ways = dict.findWays(new Vertex("соответствующий"), new Vertex("значение"), 5);
        for (List<Vertex> way : ways) {
            System.out.print(way.stream().map(vertex -> vertex.getWord().getStr()).collect(Collectors.joining("  ")));
            System.out.println("\t\t" + way.size());
        }

        Helper.printConnective(dict, "dict.txt");

        System.out.print("");
//        for (CSV_words word : words) {
//            System.out.println(word.getId() + "\t" + word.getSpelling() + "\t" + word.getPosId());
//        }
    }
}
