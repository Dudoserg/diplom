package settings;

import dict.DictException;
import dict.RelationType;
import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;

@Getter
@Setter
public class Settings {
    private Double _ASS_WEIGHT_;
    private Double _SYN_WEIGHT_;
    private Double _DEF_WEIGHT_;
    private Integer _R_;
    private Double _GAMMA_;
    private Integer _GAMMA_ATTENUATION_RATE_;


    public Settings(Double _ASS_WEIGHT_, Double _SYN_WEIGHT_, Double _DEF_WEIGHT_, Integer _R_, Double _GAMMA_, Integer _GAMMA_ATTENUATION_RATE_) {
        this._ASS_WEIGHT_ = _ASS_WEIGHT_;
        this._SYN_WEIGHT_ = _SYN_WEIGHT_;
        this._DEF_WEIGHT_ = _DEF_WEIGHT_;
        this._R_ = _R_;
        this._GAMMA_ = _GAMMA_;
        this._GAMMA_ATTENUATION_RATE_ = _GAMMA_ATTENUATION_RATE_;
    }

    public double getWeight(RelationType relationType) throws DictException {
        switch (relationType) {
            case ASS: {
                return this._ASS_WEIGHT_;
            }
            case DEF: {
                return this._DEF_WEIGHT_;
            }
            case SYN: {
                return this._SYN_WEIGHT_;
            }
            case UNKNOWN:
            default: {
                throw new DictException("unknown type of relationtype!");
            }
        }
    }

    public String getSettings() {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return "(" + "ass=" + decimalFormat.format(_ASS_WEIGHT_) + ", " +
                "def=" + decimalFormat.format(_DEF_WEIGHT_) + ", " +
                "syn=" + decimalFormat.format(_SYN_WEIGHT_) + ", " +
                "R=" + _R_ + ", " +
                "g=" + decimalFormat.format(_GAMMA_) + ", " +
                "степень=" + _GAMMA_ATTENUATION_RATE_ + ", " +
                ")";
    }
}
