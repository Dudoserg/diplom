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
public class Settings_analyzer extends Setting_Base {

    @CustomAnnotation(key = "domainPath")
    protected String domainPath = "";

    @CustomAnnotation(key = "resultPath")
    protected String resultPath = "";

    protected static Settings_analyzer instance;

    public static Settings_analyzer load(String path) throws IOException, IllegalAccessException {
        instance = new Settings_analyzer(path);
        return instance;
    }

    public static Settings_analyzer load() throws IOException, IllegalAccessException {
        instance = new Settings_analyzer("null");
        return instance;
    }

    public static Settings_analyzer getInstance() throws IOException, IllegalAccessException {
        if (instance == null) {
            instance = Settings_analyzer.load();
        }
        return instance;
    }

    private Settings_analyzer(String path) throws IOException, IllegalAccessException {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Файл настроек не найдет, будут применены стандартный настройки 'settings/setting.conf' ");
            Helper.saveToFile(Settings_analyzer.DEFAULT_SETTINGS, CONST.SETTING_DEFAULT_PATH);
            readSettingsFromFile(Helper.path("settings", "setting_default.conf"));
        } else {
            readSettingsFromFile(path);
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
                setField(fieldName, value, Settings_analyzer.class);
            }
        }
    }


    private static final String DEFAULT_SETTINGS =
            "#domainPath \tпуть к директории, в которой расположен доменный граф\n" +
                    "\n" +
                    "domainPath = bin\\output" + "\n" +
                    "resultPath = res\\pth\\result.json"
            ;
}
