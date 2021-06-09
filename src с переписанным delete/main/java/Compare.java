import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Compare {
    public static void main(String[] args) {
        List<String> c_list = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("C:\\Users\\Dudoser\\source\\repos\\test\\test\\output.txt"));
            String line = reader.readLine();
            while (line != null) {
                c_list.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> j_list = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader("C_result.txt"));
            String line = reader.readLine();
            while (line != null) {
                j_list.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0 ; i < c_list.size(); i++){
            System.out.println(c_list.get(i) + "\t\t" + j_list.get(i));
            if(!c_list.get(i).equals(j_list.get(i))){
                System.out.println("ERROR");
                break;
            }
        }
    }
}
