package SpellerChecker;

import lombok.Getter;
import lombok.Setter;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Russian;
import org.languagetool.rules.RuleMatch;
import utils.Helper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Languagetool {

    public static void main(String[] args) throws IOException, InterruptedException {
        Languagetool languagetool = new Languagetool();
        String correct = languagetool.getCorrect(Helper.readFile("SpellerChecker" + File.separator + "orf.txt"));
        System.out.println(correct);
    }

    private final static String PATH_FOLDER = "SpellerChecker";
    private final static String PATH_RESULT = PATH_FOLDER + File.separator + "yaspeller_report.json";
    private final String pathToFile;
    private final String id;

    public Languagetool() {
        this.id = UUID.randomUUID().toString();
        pathToFile = PATH_FOLDER + File.separator + id + ".txt";
    }


    public String getCorrect(String text) throws IOException, InterruptedException {
        String result = "";
       /* try {
            this.saveToFile();
            this.execute();

            String resultJson = Helper.readFile(PATH_RESULT);
            ObjectMapper objectMapper = new ObjectMapper();
            LinkedHashMap<String, Object> map =
                    (LinkedHashMap<String, Object>)
                            ((ArrayList) (objectMapper.readValue(resultJson, List.class)).get(0))
                                    .get(1);
            ArrayList data = (ArrayList) map.get("data");


            List<YaSpellerResult> errors = new ArrayList<>();
            for (Object datum : data) {
                YaSpellerResult pojo = objectMapper.convertValue(datum, YaSpellerResult.class);
                errors.add(pojo);
            }


            String[] rows = this.text.split("\r\n");

            int bias = 0;
            int lineOld = -1;
            for (YaSpellerResult error : errors) {
                String suggest = error.getSuggest().get(0);

                int line = error.getPosition().get(0).getLine();
                int column = error.getPosition().get(0).getColumn();

                String sentenceWithError = rows[line - 1];

                String start = sentenceWithError.substring(0, column - 1);
                String last = sentenceWithError.substring(column + suggest.length(), sentenceWithError.length());
                rows[line - 1] = start + suggest + last;

                bias = suggest.length() - error.getWord().length();
                System.out.println();
            }

        } catch (InterruptedException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            this.removeFile();
        }*/

        JLanguageTool langTool = new JLanguageTool(new Russian());
        // comment in to use statistical ngram data:
        //langTool.activateLanguageModelRules(new File("/data/google-ngram-data"));
        int prevPos = 0;
        int currentPos = 0;
        List<RuleMatch> matches = langTool.check(text);
        for (RuleMatch match : matches) {
//            System.out.println("Potential error at characters " +
//                    match.getFromPos() + "-" + match.getToPos() + ": " +
//                    match.getMessage());
//            System.out.println("Suggested correction(s): " +
//                    match.getSuggestedReplacements().get(0));

//            if (match.getMessage().contains("Возможно найдена орфографическая ошибка")) {
                result += text.substring(prevPos, match.getFromPos());
                result += match.getSuggestedReplacements().get(0);
                prevPos = match.getToPos();
//            }

        }
        result += text.substring(prevPos, text.length());

        return result;
    }

    private void execute() throws IOException, InterruptedException {
//            String command = "cd PATH_FOLDER" + File.separator + "yaspeller " + id + ".txt " + "--report json";
        String command = PATH_FOLDER + File.separator + "start.bat";
        Process p = Runtime.getRuntime()
                .exec(command);
        p.waitFor();
    }

    private boolean removeFile() {
        File f = new File(pathToFile);
        return f.delete();
    }

    private void saveToFile() throws IOException {
    }
}
