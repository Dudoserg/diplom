package settings;

import utils.Helper;

import java.io.IOException;

public class SettingsLoader {
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

    public SettingsLoader(String path) {
    }

    // Дефолтные настройки, пересоздаем файл настроек, если его кто то удалил
    public SettingsLoader() throws IOException {
        Helper.saveToFile(SettingsLoader.DEFAULT_SETTINGS, Helper.path("settings"));

    }
}
