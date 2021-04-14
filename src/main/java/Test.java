import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) {

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 500000; i++) {
            int rndm = rndm(-1000, 1000);
            list.add(rndm);
        }

        try (FileWriter writer = new FileWriter("C.txt", false)) {
            // запись всей строки
            String str = list.stream().map(integer -> String.valueOf(integer))
                    .collect(Collectors.joining("\n"));
            writer.write(list.size() + "\n");
            writer.write(str);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        Collections.sort(list);
        try (FileWriter writer = new FileWriter("C_result.txt", false)) {
            // запись всей строки
            String str = list.stream().map(integer -> String.valueOf(integer))
                    .collect(Collectors.joining("\n"));
            writer.write(str);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static int rndm(int a, int b) {
        return a + (int) (Math.random() * ((b - a) + 1));
    }
}
