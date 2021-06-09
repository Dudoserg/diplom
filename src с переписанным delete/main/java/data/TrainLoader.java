package data;

import utils.Helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainLoader {
    public TrainLoader() {
    }


    public List<String> load(String path) throws IOException {
        File pth = new File(path);

        boolean exists =      pth.exists();      // Check if the file exists
        boolean isDirectory = pth.isDirectory(); // Check if it's a directory
        boolean isFile =      pth.isFile();      // Check if it's a regular file

        if(isDirectory){
            File dir = new File(path); //path указывает на директорию
            File[] arrFiles = dir.listFiles();
            List<File> lst = Arrays.asList(arrFiles);

            List<String> result = new ArrayList<>();
            for (File file : lst) {
                String s = Helper.readFile(file.getAbsolutePath());
                s = s.replace("\r\n", " ");
                s = s.replace("\n", " ");
                result.add(s);
            }
            System.out.println("training sample load " + result.size() + " files");
            return result;
        } else if( isFile){
            String s = Helper.readFile(pth.getAbsolutePath());
            s = s.replace("\r\n", " ");
            s = s.replace("\n", " ");
            List<String> result = new ArrayList<>();
            result.add(s);
            System.out.println("training sample load " + result.size() + " files");
            return result;
        }else {
            throw new IOException("[" + path + "] is wrong path!");
        }

    }
}
