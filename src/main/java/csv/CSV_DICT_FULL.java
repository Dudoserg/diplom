package csv;

import dict.PartOfSpeech;
import dict.RelationType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CSV_DICT_FULL {
    private List<CSV_DICT_ROW> list = new ArrayList<>();

    public void addRow(CSV_words wordFrom, CSV_words wordTo, RelationType relationType) {
        PartOfSpeech fromPartOfSpeec = null;
        PartOfSpeech toPartOfSpeec = null;

        list.add(new CSV_DICT_ROW(wordFrom.getSpelling(), null,  wordTo.getSpelling(),null, relationType));

    }

}
