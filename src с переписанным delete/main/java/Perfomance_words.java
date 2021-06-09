
import utils.Helper;
import utils.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Perfomance_words {
    public static void main(String[] args) throws Exception {
        Perfomance_words perfomance = new Perfomance_words();

        ///////////////////////////////////////
        ///////////////////////////////////////
        /// неверное название было 56 на 57 поменяли
//        File dir = new File(perfomance.dirPath + File.separator + "test"); //path указывает на директорию
//        File[] arrFiles = dir.listFiles();
//        List<File> lst = Arrays.asList(arrFiles);
//        lst = lst.stream().filter(file -> file.getName().contains("56")).collect(Collectors.toList());
//        for (File file : lst) {
//            String name = file.getName();
//            name = name.replace("56", "57");
//            Path source = Paths.get(file.getAbsolutePath());
//            Files.move(source, source.resolveSibling(name));
//        }
//        if(true)
//            return;

        ///////////////////////////////////////
        ///////////////////////////////////////
        // Извлекаем уникальные слова из результатов
        if (false) {
            Set<String> strings = perfomance.calculateUnicalWord();
            for (String string : strings) {
                System.out.println(string);
            }
        }

        ///////////////////////////////////////
        ///////////////////////////////////////
        // Из файла, с проставленными вручную настройками, узвлекаем слова с оценками
        if (false) {
            for (String s : Helper.readFileLineByLine(perfomance.dirPath + File.separator + "expert_mark.txt")) {
                String[] split = s.split("\\s+");
                if (split.length > 1) {
                    if (perfomance.isNumeric(split[1])) {
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
            Map<String, Integer> marks = perfomance.readMarks();
            Map<String, Double> results = perfomance.calculateRating(marks);

            List<Pair<String, Double>> list = new ArrayList<>();
            for (Map.Entry<String, Double> elem : results.entrySet()) {
                list.add(new Pair<>(elem.getKey(), elem.getValue()));
            }
            Collections.sort(list, (first, second) -> -Double.compare(first.getSecond(), second.getSecond()));

            for (Pair<String, Double> stringDoublePair : list) {
                System.out.println(stringDoublePair.getFirst() + "\t\t" + stringDoublePair.getSecond());
            }
            String collect = list.stream()
                    .map(stringDoublePair -> stringDoublePair.getFirst() + "\t" + stringDoublePair.getSecond())
                    .collect(Collectors.joining("\n"));
            Helper.saveToFile(collect, perfomance.dirPath + File.separator + "rating.txt");
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
                            "A" + a + "_" + "S" + s + "_" + "D" + d + ".txt";
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
                            "A" + a + "_" + "S" + s + "_" + "D" + d + ".txt";
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
                    fileMark.put("A" + a + "_" + "S" + s + "_" + "D" + d + ".txt", rating);
                }
            }
        }

        return fileMark;
    }

}
