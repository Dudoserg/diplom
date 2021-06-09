package settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import dict.DictException;
import dict.RelationType;
import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class Settings {
    @CustomAnnotation(key = "assoc")
    private Double _ASS_WEIGHT_;

    @CustomAnnotation(key = "syn")
    private Double _SYN_WEIGHT_;

    @CustomAnnotation(key = "def")
    private Double _DEF_WEIGHT_;

    @CustomAnnotation(key = "r")
    private Integer _R_;

    @CustomAnnotation(key = "gamma")
    private Double _GAMMA_;

    @CustomAnnotation(key = "gamma_attenuation_rate")
    private Integer _GAMMA_ATTENUATION_RATE_;

    @CustomAnnotation(key = "countThreads")
    private int countThreads = 4;

    private static Settings instance;

    public static Settings load(String path) throws IOException, IllegalAccessException {
        instance = new Settings(path);
        return instance;
    }

    public static Settings load() throws IOException, IllegalAccessException {
        instance = new Settings("null");
        return instance;
    }

    public static Settings getInstance() throws IOException, IllegalAccessException {
        if (instance == null) {
            instance = Settings.load();
        }
        return instance;
    }

    private Settings(String path) throws IOException, IllegalAccessException {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Файл настроек не найдет, будут применены стандартный настройки 'settings/setting.conf' ");
            Helper.saveToFile(Settings.DEFAULT_SETTINGS, Helper.path("settings", "setting_default.conf"));
            readSettingsFromFile(Helper.path("settings", "setting_default.conf"));
        } else {
            readSettingsFromFile(path);
        }
    }

    private Settings() {
    }


//    public Settings(Double _ASS_WEIGHT_, Double _SYN_WEIGHT_, Double _DEF_WEIGHT_, Integer _R_, Double _GAMMA_, Integer _GAMMA_ATTENUATION_RATE_) {
//        this._ASS_WEIGHT_ = _ASS_WEIGHT_;
//        this._SYN_WEIGHT_ = _SYN_WEIGHT_;
//        this._DEF_WEIGHT_ = _DEF_WEIGHT_;
//        this._R_ = _R_;
//        this._GAMMA_ = _GAMMA_;
//        this._GAMMA_ATTENUATION_RATE_ = _GAMMA_ATTENUATION_RATE_;
//    }

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

    //    public void loadDefault() {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("association = ");
//    }
//
    private void readSettingsFromFile(String path) throws IllegalAccessException {
        List<String> strings = Helper.readFileLineByLine(path);
        List<String> collect = strings.stream().filter(s -> s.length() > 0 && s.charAt(0) != '#')
                .collect(Collectors.toList());
        for (String s : collect) {
            s = s.trim().replaceAll("\\t", "");
            s = s.trim().replaceAll(" +", "");
            if (s.contains("#"))
                s = s.substring(0, s.indexOf('#'));
            String[] split = s.split("=");
            if (split.length == 2) {
                String fieldName = split[0];
                String value = split[1];
                this.setField(fieldName, value);
            }
        }
    }

    private void setField(String fieldName, String value) throws IllegalAccessException {
        for (Field f : Settings.class.getDeclaredFields()) {
            CustomAnnotation column = f.getAnnotation(CustomAnnotation.class);
            if (column != null) {
//                System.out.println(column.key());
                String key = column.key();
                if (key.equals(fieldName)) {
                    Class<?> type = f.getType();
                    if (type.equals(int.class)) {
                        f.set(this, Integer.valueOf(value));
                    } else if (type.equals(Integer.class)) {
                        f.set(this, Integer.valueOf(value));
                    } else if (type.equals(double.class)) {
                        f.set(this, Double.parseDouble(value));
                    } else if (type.equals(Double.class)) {
                        f.set(this, Double.parseDouble(value));
                    } else if (type.equals(String.class)) {
                        f.set(this, value);
                    }
                }
            }
        }
    }

    private static final String DEFAULT_SETTINGS =
            "#assoc = 0.15\t\t\t\t\tвес связи ассоциации\n" +
                    "#syn = 0.15\t\t\t\t\t\tвес связи синонимии\n" +
                    "#def = 0.3\t\t\t\t\t\tвес связи определения\n" +
                    "#gamma = 0.65\t\t\t\t\tкоэффициент затухания\n" +
                    "#r = 3\t\t\t\t\t\t\tрадиус рассмотрения вершин\n" +
                    "#gamma_attenuation_rate = 3\t\tстепень функции затухания(квадратичная, кубическая(0,1,2,3))\n" +
                    "#countThreads = 4\t\t\t\tколичество потоков используемых программой\n" +
                    "\n" +
                    "assoc = 0.15\n" +
                    "syn = 0.15\n" +
                    "def = 0.3\n" +
                    "gamma = 0.65\n" +
                    "r = 3\n" +
                    "gamma_attenuation_rate = 3\n" +
                    "countThreads = 4";
}
