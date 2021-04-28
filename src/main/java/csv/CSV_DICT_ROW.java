package csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import dict.PartOfSpeech;
import dict.RelationType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CSV_DICT_ROW {
    @JsonProperty("f")
    private String from;
    @JsonProperty("fp")
    private PartOfSpeech fromPartOfSpeech;

    @JsonProperty("t")
    private String to;
    @JsonProperty("tp")
    private PartOfSpeech toPartOfSpeech;

    @JsonProperty("r")
    private RelationType relationType;

    public CSV_DICT_ROW(String from, PartOfSpeech fromPartOfSpeech, String to, PartOfSpeech toPartOfSpeech, RelationType relationType) {
        this.from = from;
        this.fromPartOfSpeech = fromPartOfSpeech;
        this.to = to;
        this.toPartOfSpeech = toPartOfSpeech;
        this.relationType = relationType;
    }
}
