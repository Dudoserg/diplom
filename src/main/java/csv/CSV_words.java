package csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CSV_words {

    @CsvBindByName(column = "id")
//    @CsvBindByPosition(position = 0)
    private int id;

    @CsvBindByName(column = "spelling")
    //@CsvBindByPosition(position = 1)
    private String spelling;

    @CsvBindByName(column = "pos_id")
    //@CsvBindByPosition(position = 2)
    private int posId;

}
