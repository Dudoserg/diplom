package settings;

import constant.CONST;
import dict.DictException;
import dict.RelationType;
import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Settings_constructor extends Setting_Base {
    @CustomAnnotation(key = "assoc")
    protected Double _ASS_WEIGHT_;

    @CustomAnnotation(key = "syn")
    protected Double _SYN_WEIGHT_;

    @CustomAnnotation(key = "def")
    protected Double _DEF_WEIGHT_;

    @CustomAnnotation(key = "r")
    protected Integer _R_;

    @CustomAnnotation(key = "gamma")
    protected Double _GAMMA_;

    @CustomAnnotation(key = "gamma_attenuation_rate")
    protected Integer _GAMMA_ATTENUATION_RATE_;

    @CustomAnnotation(key = "countThreads")
    protected int countThreads = 4;


    @CustomAnnotation(key = "outputPath")
    protected String outputPath = "";

    protected static Settings_constructor instance;

    public static Settings_constructor load(String path) throws IOException, IllegalAccessException {
        instance = new Settings_constructor(path);
        return instance;
    }

    public static Settings_constructor load() throws IOException, IllegalAccessException {
        instance = new Settings_constructor("null");
        return instance;
    }

    public static Settings_constructor getInstance() throws IOException, IllegalAccessException {
        if (instance == null) {
            instance = Settings_constructor.load();
        }
        return instance;
    }

    private Settings_constructor(String path) throws IOException, IllegalAccessException {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Файл настроек не найдет, будут применены стандартный настройки 'settings/setting.conf' ");
            Helper.saveToFile(Settings_constructor.DEFAULT_SETTINGS, CONST.SETTING_DEFAULT_PATH);
            readSettingsFromFile(Helper.path("settings", "setting_default.conf"));
        } else {
            readSettingsFromFile(path);
        }
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
                setField(fieldName, value, Settings_constructor.class);
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
                    "countThreads = 4\n" +
                    "outputPath = bin\\output"
            ;
}
