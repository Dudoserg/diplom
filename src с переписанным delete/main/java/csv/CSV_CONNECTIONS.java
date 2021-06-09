package csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByName;
import dict.PartOfSpeech;
import dict.RelationType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CSV_CONNECTIONS {
    @CsvBindByName(column = "ID")
//    @CsvBindByPosition(position = 0)
    private int id;

    @CsvBindByName(column = "WORD_FROM")
//    @CsvBindByPosition(position = 1)
    private String wordFrom;

    @CsvBindByName(column = "PARTOFSPEECHFROM")
//    @CsvBindByPosition(position = 2)
    private PartOfSpeech partOfSpeechFrom;

    @CsvBindByName(column = "WORD_TO")
//    @CsvBindByPosition(position = 3)
    private String wordTo;

    @CsvBindByName(column = "PARTOFSPEECHTO")
//    @CsvBindByPosition(position = 4)
    private PartOfSpeech partOfSpeechTo;

    @CsvBindByName(column = "RELATIONTYPE")
//    @CsvBindByPosition(position = 5)
    private RelationType relationType;

    public static CSV_CONNECTIONS create(int id, String wordFrom, PartOfSpeech partOfSpeechFrom, String wordTo, PartOfSpeech partOfSpeechTo, RelationType relationType) {
        CSV_CONNECTIONS csv_connections = new CSV_CONNECTIONS();
        csv_connections.id = id;
        csv_connections.wordFrom = wordFrom;
        csv_connections.partOfSpeechFrom = partOfSpeechFrom;
        csv_connections.wordTo = wordTo;
        csv_connections.partOfSpeechTo = partOfSpeechTo;
        csv_connections.relationType = relationType;
        return csv_connections;
    }
}
