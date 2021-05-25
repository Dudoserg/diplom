package mystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Getter
@Setter
public class Mystem {
    private final String MYSTEM_exe = "mystem" + File.separator + "exe" + File.separator + "mystem.exe";
    private String TMP_PATH = "mystem" + File.separator + "tmp";
    private String MYSTEM_RESULT_json;
    public String FULL_TEXT;

    private final String id;

    public Mystem() {
        id = UUID.randomUUID().toString();
        MYSTEM_RESULT_json = TMP_PATH + File.separator + "mystemResult_" + id + ".json";
        File tmpDir = new File(TMP_PATH);
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
        FULL_TEXT = TMP_PATH + File.separator + "full_text_" + id + ".txt";
    }

    public MyStemResult analyze(String text) throws IOException, InterruptedException {

        text = text.replace("\r\n", " ");
        text = text.replace("\n", " ");
        text = text.replace(".", ". ");
        text = text.replace(",", ", ");
        text = text.replace("!", "! ");
        text = text.replace("?", "? ");
        text = text.replace("-", "? ");
        text.replace(System.lineSeparator(), " ");
        text = text.trim().replaceAll("\\s{2,}", " ");
        text = text.toLowerCase();
        // save text TO FULL FILE
        saveToFile(text, FULL_TEXT);
        // analyze
        MyStemResult myStemResult = null;
        try {
            String command = MYSTEM_exe + " " + FULL_TEXT + " " + MYSTEM_RESULT_json + " " +
                    "--format json -c -l -s  -i";
            Process p = Runtime.getRuntime()
                    .exec(command);
            p.waitFor();
            String json = readFile(MYSTEM_RESULT_json);

            ObjectMapper objectMapper = new ObjectMapper();

            // читаем результаты работы лемманизатора MyStem
            MyStemItem[] myStemItems = objectMapper.readValue(json, MyStemItem[].class);
            ArrayList<MyStemItem> objects = new ArrayList<>(Arrays.asList(myStemItems));
            objects.forEach(MyStemItem::calcPartOfSpeech);
            myStemResult = new MyStemResult(objects);
        } catch (Exception ex) {
            throw ex;
        } finally {
            // remove tmp file
            removeFIle(FULL_TEXT);
            removeFIle(MYSTEM_RESULT_json);
            return myStemResult;
        }
    }

    public MyStemResult analyze(File file) throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileReader fr = new FileReader(file);
            //создаем BufferedReader с существующего FileReader для построчного считывания
            BufferedReader reader = new BufferedReader(fr);
            // считаем сначала первую строку
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line.replace(System.lineSeparator(), " ");
                line = line.trim().replaceAll("\\s{2,}", " ");
                stringBuilder.append(line);
                // считываем остальные строки в цикле
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return analyze(stringBuilder.toString());
    }

    private boolean removeFIle(String name) {
        File f = new File(name);
        return f.delete();
    }

    private void saveToFile(String text, String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(text);
        writer.close();
    }

    private String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8.name());
    }
}
