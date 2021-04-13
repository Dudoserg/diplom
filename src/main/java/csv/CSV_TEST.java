package csv;

import com.opencsv.bean.CsvToBeanBuilder;
import dict.*;
import dict.Edge.Edge;

import java.io.File;
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

            double weight = 0.0;
            RelationType relationType = null;

            if(con.getDefWeight() > 0){
                weight = con.getDefWeight();
                relationType = RelationType.DEF;
            }else if(con.getAssWeight() > 0){
                weight = con.getAssWeight();
                relationType = RelationType.ASS;
            }else if(con.getSynWeight() > 0){
                weight = con.getSynWeight();
                relationType = RelationType.SYN;
            }else {
                throw new DictException("weight of edge equals 0.0 [id=" + con.getId() + "]");
            }
            dict.addPair(wordFrom.getSpelling(), wordTo.getSpelling(), weight, relationType);

            Word.getWord(wordFrom.getSpelling()).setWordType(WordType.create(wordFrom.getPosId()));
            Word.getWord(wordTo.getSpelling()).setWordType(WordType.create(wordTo.getPosId()));
        }
        dict.addPair("соответствующий", "значение", 0.0, RelationType.DEF);

//        System.out.print("\n\n\n");
//        List<List<Vertex>> ways = dict.findWays(new Vertex("соответствующий"), new Vertex("значение"), 5);
//        for (List<Vertex> way : ways) {
//            System.out.print(way.stream().map(vertex -> vertex.getWord().getStr()).collect(Collectors.joining("  ")));
//            System.out.println("\t\t" + way.size());
//        }
//
//        Helper.printConnective(dict, "dict.txt");
//
//        System.out.print("");
    }
DictBase dictBase;

    void funWeight(Vertex v, double weightAdd, int radius, double gamma) {
        if (weightAdd == 0) return;
        v.setWeight(v.getWeight() + weightAdd);
        for (Map.Entry<Vertex, Edge> neighbour : dictBase.getNeighbours(v).getEdgeMap().entrySet()) {
            Edge edge = neighbour.getValue();
            Vertex v2 = neighbour.getKey();
            funWeight(v2, weightAdd * gamma * edge.getWeight(), radius - 1, gamma * gamma);
        }
    }
}
