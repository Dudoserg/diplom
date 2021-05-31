import javafx.util.Pair;
import utils.Helper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Perfomance_clusters {
    public static void main(String[] args) throws Exception {
        Perfomance_clusters perfomanceClusters = new Perfomance_clusters();


        ///////////////////////////////////////
        ///////////////////////////////////////
        // Извлекаем уникальные слова из результатов
        if (false) {
            Set<String> strings = perfomanceClusters.calculateUnicalWord();
            for (String string : strings) {
                System.out.println(string);
            }
        }

        ///////////////////////////////////////
        ///////////////////////////////////////
        // Из файла, с проставленными вручную настройками, узвлекаем слова с оценками
        if (false) {
            for (String s : Helper.readFileLineByLine(perfomanceClusters.dirPath + File.separator + "expert_mark.txt")) {
                String[] split = s.split("\\s+");
                if (split.length > 1) {
                    if (perfomanceClusters.isNumeric(split[1])) {
                        Integer integer = Integer.valueOf(split[1]);
                        if (integer > 0) {
                            System.out.println(s);
                        }
                    }
                }
            }
        }
        ///////////////////////////////////////
        ///////////////////////////////////////
        // рейтинг файлов
        if (true) {
            Map<String, Integer> marks = perfomanceClusters.readMarks();
            Map<String, Double> results = perfomanceClusters.calculateRating(marks);

            List<Pair<String, Double>> list = new ArrayList<>();
            for (Map.Entry<String, Double> elem : results.entrySet()) {
                list.add(new Pair<>(elem.getKey(), elem.getValue()));
            }
            Collections.sort(list, (first, second) -> -Double.compare(first.getValue(), second.getValue()));

            for (Pair<String, Double> stringDoublePair : list) {
                System.out.println(stringDoublePair.getKey() + "\t\t" + stringDoublePair.getValue());
            }
            String collect = list.stream()
                    .map(stringDoublePair -> stringDoublePair.getKey() + "\t" + stringDoublePair.getValue())
                    .collect(Collectors.joining("\n"));
            Helper.saveToFile(collect, perfomanceClusters.dirPath + File.separator + "rating_cluster.txt");
            System.out.println();
        }

    }

    final String dirPath = "C:" + File.separator + "Users" + File.separator + "Dudoser" + File.separator + "Desktop" +
            File.separator + "results";

    private boolean isFileExist(String fileName) {
        File tempFile = new File(fileName);
        return tempFile.exists();
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Set<String> calculateUnicalWord() throws IOException {
        Set<String> wordsSet = new TreeSet<>();
        for (int a = 0; a < 100; a += 1) {
            System.out.println("a = " + a);
            for (int s = 0; s < 100; s += 1) {
                for (int d = 0; d < 100; d += 1) {
                    String fileName = dirPath + File.separator + "test" + File.separator +
                            "A" + a + "_" + "S" + s + "_" + "D" + d + "_cluster.txt";
                    if (!isFileExist(fileName))
                        continue;
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
        List<String> s = Helper.readFileLineByLine(dirPath + File.separator + "marks.txt");
        for (String row : s) {
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
        for (int a = 0; a < 100; a += 1) {
            System.out.println("a = " + a);
            for (int s = 0; s < 100; s += 1) {
                for (int d = 0; d < 100; d += 1) {
                    String fileName = dirPath + File.separator + "test" + File.separator +
                            "A" + a + "_" + "S" + s + "_" + "D" + d + "_cluster.txt";
                    if (!isFileExist(fileName))
                        continue;
                    String result = Helper.readFile(fileName);
                    String[] fileRows = result.split("\n");
                    Double rating = 0.0;
                    for (int i = 0; i < 100; i++) {
                        String[] split = fileRows[i].split("\\s+");
                        String word = split[1];
                        Integer markValue = marks.get(word);
                        if (markValue != null && markValue > 0) {
                            rating += markValue * (100.0 - i);
                        } else {
                        }
                    }
                    fileMark.put("A" + a + "_" + "S" + s + "_" + "D" + d + "_cluster.txt", rating);
                }
            }
        }

        return fileMark;
    }

}
