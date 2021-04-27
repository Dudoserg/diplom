package settings;

import lombok.Getter;
import lombok.Setter;

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
}
