package csv;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CSV_connections {
    @CsvBindByName(column = "id")
//    @CsvBindByPosition(position = 0)
    private int id;


    @CsvBindByName(column = "word_from")
//    @CsvBindByPosition(position = 0)
    private int wordFrom;

    @CsvBindByName(column = "word_to")
//    @CsvBindByPosition(position = 0)
    private int wordTo;


    @CsvBindByName(column = "def_weight")
//    @CsvBindByPosition(position = 0)
    private double defWeight;


    @CsvBindByName(column = "syn_weight")
//    @CsvBindByPosition(position = 0)
    private double synWeight;


    @CsvBindByName(column = "assoc_weight")
//    @CsvBindByPosition(position = 0)
    private double assWeight;
}
