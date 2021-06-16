import utils.Helper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CodeReport {
    public static void main(String[] args) {
        CodeReport codeReport = new CodeReport();
        List<File> lst = getFiles(Helper.path("src", "main", "java"));
        codeReport.start(lst, 0);

        System.out.println("countFiles = " + codeReport.countFiles);
        System.out.println("countDirs = " + codeReport.countDirs);

        System.out.println("\n\n");
        System.out.println(codeReport.result);
    }
    int countFiles = 0;
    int countDirs = 0;
    String result = "";

    private void start(List<File> lst, int level) {
        String space = "";
        for (int i = 0; i < level; i++)
            space += "\t";

        List<File> files = lst.stream().filter(file -> file.isFile()).collect(Collectors.toList());
        List<File> dirs = lst.stream().filter(file -> file.isDirectory()).collect(Collectors.toList());

        for (File file : files) {
            countFiles++;
            System.out.println(space + "[f]" + file.getName() + "\t" + file.getAbsolutePath());
            List<String> fileLines = Helper.readFileLineByLine(file.getAbsolutePath());
            String fileText = fileLines.stream().collect(Collectors.joining("\n"));
            result += file.getName() +  "\n\n" +  fileText + "\n" +
                    "====================================================\n\n";
        }
        for (File dir : dirs) {
            countDirs++;
            System.out.println(space + "\t[d]" + dir.getName() + " : ");
            List<File> tmp = getFiles(dir.getAbsolutePath());




            start(tmp, level + 1);
        }
    }



    public static List<File> getFiles(String path) {
        File dir = new File(path); //path указывает на директорию
        File[] arrFiles = dir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        return lst;
    }
}
