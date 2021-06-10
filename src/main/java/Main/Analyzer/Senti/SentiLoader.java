package Main.Analyzer.Senti;

import constant.CONST;
import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.util.List;

@Getter
@Setter
public class SentiLoader implements SentiLoaderInterface{

    private static final String PATH = CONST.SENTI_DICTIONARY_PATH;

    @Override
    public SentimentDictionary load() {
        List<String> strings = Helper.readFileLineByLine(PATH);
        strings.remove(0);
        SentimentDictionary dictionary = new SentimentDictionary();
        for (String string : strings) {
            String[] split = string.split(";");
            String term = split[0];
            String tagStr = split[1];
            double value = Double.parseDouble(split[2]);
            double pstv = Double.parseDouble(split[3]);
            double ngtv = Double.parseDouble(split[4]);
            double neut = Double.parseDouble(split[5]);
            double dunno = Double.parseDouble(split[6]);
            double pstvNgtvDisagreementRatio = Double.parseDouble(split[7]);

            SentiValue sentiValue = new SentiValue(
                    term, SentiTag.getPart(tagStr), value, pstv, ngtv, neut, dunno, pstvNgtvDisagreementRatio
            );
            dictionary.addRow(term, sentiValue);
        }
        return dictionary;
    }
}
