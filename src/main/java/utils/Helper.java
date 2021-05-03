package utils;

import dict.DictBase;
import dict.Edge.Edge;
import dict.EdgeMap;
import dict.Vertex;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Helper {


    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8.name());
    }

    public static List<String> readFileLineByLine(String path) {
        BufferedReader reader;
        List<String> lines = new ArrayList<>();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static void saveToFile(String text, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(text);
        writer.close();
    }

    public static void printUnigram(Map<Unigram, Integer> map, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        List<Unigram> unigramList = map.entrySet().stream()
                .map(unigramIntegerEntry -> {
                    Unigram key = unigramIntegerEntry.getKey();
                    key.setFrequency(unigramIntegerEntry.getValue());
                    return key;
                })
                .collect(Collectors.toList());

        unigramList.sort((o1, o2) -> -Integer.compare(o1.getFrequency(), o2.getFrequency()));
        Integer integer = unigramList.stream()
                .map(unigram -> unigram.getFirst().length())
                .sorted((o1, o2) -> -Integer.compare(o1, o2))
                .findFirst().orElse(0);
        for (Unigram unigram : unigramList) {
            writer.write("[" + unigram.getFirst() + "]" + "\t" +
//                    new String(new char[integer - unigram.getFirst().length() + 10]).replace('\0', ' ') +
                    unigram.getFrequency() + "\n");

        }
        writer.close();
    }

    public static void printBigram(Map<Bigram, Integer> map, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        List<Bigram> bigramList = map.entrySet().stream()
                .map(unigramIntegerEntry -> {
                    Bigram key = unigramIntegerEntry.getKey();
                    key.setFrequency(unigramIntegerEntry.getValue());
                    return key;
                })
                .collect(Collectors.toList());

        bigramList.sort((o1, o2) -> -Integer.compare(o1.getFrequency(), o2.getFrequency()));

        Integer integer = bigramList.stream()
                .map(bigram -> bigram.getFirst().length() + bigram.getSecond().length())
                .sorted((o1, o2) -> -Integer.compare(o1, o2))
                .findFirst().orElse(0);

        for (Bigram bigram : bigramList) {
            writer.write("[" + bigram.getFirst() + "]" +
                    "[" + bigram.getSecond() + "]" + "\t" +
//                    new String(new char[integer - bigram.getFirst().length() - bigram.getSecond().length() + 10]).replace('\0', ' ') +
                    bigram.getFrequency() + "\n");

        }
        writer.close();
    }


    public static void printConnective(DictBase dict, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));

        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dict.getMap().entrySet()) {
            Vertex v = vertexEdgeMapEntry.getKey();
            EdgeMap emap = vertexEdgeMapEntry.getValue();
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : emap.getEdgeMap().entrySet()) {
                Vertex v2 = vertexEdgeEntry.getKey();
                Edge e = vertexEdgeEntry.getValue();
//                System.out.println(v.getWord().getStr() + "\t" + v.getWord().getStr() + "\t" +
//                        "a=" +e.getAss_weight() + "   d=" + e.getDef_weight() + "   s=" + e.getSyn_weight());
                writer.write(v.getWord().getStr() + "__" + v2.getWord().getStr() + " " +
                        "w=" + e.getWeight() + " " + " r=" + e.getRelationType() + "\n");
            }
        }
        writer.close();
    }

    public static String path(String... str) {
        return Arrays.stream(str).collect(Collectors.joining(File.separator));
    }
}
