package Main.Analyzer.Senti;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentiValue {
    private String term;
    private SentiTag tag;

    private double value;
    private double pstv;
    private double ngtv;
    private double neut;
    private double dunno;
    private double pstvNgtvDisagreementRatio;

    public SentiValue(String term, SentiTag tag, double value, double pstv, double ngtv, double neut, double dunno, double pstvNgtvDisagreementRatio) {
        this.term = term;
        this.tag = tag;
        this.value = value;
        this.pstv = pstv;
        this.ngtv = ngtv;
        this.neut = neut;
        this.dunno = dunno;
        this.pstvNgtvDisagreementRatio = pstvNgtvDisagreementRatio;
    }
}
