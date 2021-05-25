import javafx.util.Pair;
import jdk.nashorn.internal.runtime.arrays.ArrayIndex;
import utils.Helper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Perfomance {
    public static void main(String[] args) throws Exception {
        Perfomance perfomance = new Perfomance();
        //Set<String> unicalWords = perfomance.calculateUnicalWord();
        Map<String, Integer> marks = perfomance.readMarks();
        Map<String, Double> results = perfomance.calculateRating(marks);

        List<Pair<String, Double>> list = new ArrayList<>();
        for (Map.Entry<String, Double> elem : results.entrySet()) {
            list.add(new Pair<>(elem.getKey(), elem.getValue()));
        }
        Collections.sort(list, (first, second) -> -Double.compare(first.getValue(), second.getValue()));

        for (Pair<String, Double> stringDoublePair : list) {
            System.out.println(stringDoublePair.getKey() + "\t\t" + stringDoublePair.getValue());
        }

        System.out.println();
    }

    final String dirPath = "test" + File.separator;

    public Set<String> calculateUnicalWord() throws IOException {
        Set<String> wordsSet = new TreeSet<>();
        for (int a = 10; a < 100; a += 10) {
            for (int s = 10; s < 100; s += 10) {
                for (int d = 10; d < 100; d += 10) {
                    String fileName = dirPath + "A" + a + "_" + "S" + s + "_" + "D" + d + ".txt";
                    String result = Helper.readFile(fileName);
                    String[] rows = result.split("\n");
                    for (String row : rows) {
                        String[] split = row.split("\\s+");
                        String str = split[1];
                        wordsSet.add(split[1]);
                    }

                }
            }
        }
//        for (String s : wordsSet) {
//            System.out.println(s);
//        }
        return wordsSet;
    }


    public Map<String, Integer> readMarks() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        String s = Helper.readFile(dirPath + "expert_mark.txt");
        String[] rows = s.split("\n");
        for (String row : rows) {
            String[] split = row.split("\\s+");
            try {
                map.put(split[0], Integer.valueOf(split[1]));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw e;
            }
        }
        return map;
    }

    public Map<String, Double> calculateRating(Map<String, Integer> marks) throws Exception {
        Map<String, Double> fileMark = new HashMap<>();

        File myFolder = new File("test");
        File[] files = myFolder.listFiles();
        Set<String> unknownWord = new TreeSet<>();
        for (File file : files) {
          /*  String a, s, d;
            a = s = d = "";
            {

                name = name.replaceAll("\\D", " ");
                name = name.trim().replaceAll(" +", " ");
                ;
                String[] split = name.split("\\s+");
                try {
                    a = split[0];
                    s = split[1];
                    d = split[2];
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println();
                }
            }*/
            String name = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\") + 1);

            String fileName = dirPath + name;
            if (fileName.contains("expert"))
                continue;
            String result = Helper.readFile(fileName);
            String[] rows = result.split("\n");
            int x = 0;
            double rating = 0.0;
            for (String row : rows) {
                String[] split = row.split("\\s+");
                String str = split[1];
                Integer strMark = marks.get(str);
                if (strMark == null) {
                    unknownWord.add(str);
                    strMark = 0;
                }
                rating += strMark * (100 - x);
                x++;
            }
            fileMark.put(name, rating);

        }
        if (unknownWord.size() != 0) {
            System.out.println("unknown word:");
            for (String s : unknownWord) {
                System.out.println(s);
            }
        }

        return fileMark;
    }

}
