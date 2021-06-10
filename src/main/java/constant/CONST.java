package constant;

import utils.Helper;

import java.io.File;

public class CONST {
    public final static String SETTING_STOPWORDS_PATH = Helper.path("mystem", "stopWords", "stop_words2.txt");

    public final static String SETTING_ANALYZER_DEFAULT_PATH = Helper.path("settings", "setting_analyzer_default.conf");
    public final static String SETTING_CONSTRUCTOR_DEFAULT_PATH = Helper.path("settings", "setting_constructor_default.conf");

    public final static String SETTING_BIN_ANALYZER_DEFAULT_PATH = Helper.path("bin", "settings", "setting_analyzer_default.conf");
    public final static String SETTING_BIN_CONSTRUCTOR_DEFAULT_PATH = Helper.path("bin", "settings", "setting_constructor_default.conf");

    public static final String DICTIONARY_PATH = "bin" + File.separator + "connections3.csv";

    public static final String SENTI_DICTIONARY_PATH = Helper.path("bin", "sentiDictionary.csv");
}
