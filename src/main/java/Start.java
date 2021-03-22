import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.model.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import static guru.nidi.graphviz.model.Factory.node;

public class Start {
    public Start() {
        Dict dict = readDictFromFile();
        Dict subDict = dict.getSubDict(new Word("афиша"), 1);
        System.out.printf("");

        Node link = node("a").with(Color.BLACK).link(node("b"));


        try {
            Dict.draw(dict.getGraphViz(), "example/main.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 1 ; i < 10; i++){
            try {
                Dict.draw(dict.getSubDict(new Word("афиша"), i).getGraphViz(), "example/subDict_" + i + ".png");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public Dict readDictFromFile() {
        Dict dict = new Dict(new HashMap<>());
        int countLine = 0;
        try {
            File file = new File("inputDict.txt");
            //создаем объект FileReader для объекта File
            FileReader fr = new FileReader(file);
            //создаем BufferedReader с существующего FileReader для построчного считывания
            BufferedReader reader = new BufferedReader(fr);
            // считаем сначала первую строку
            String line = reader.readLine();
            while (line != null) {
                line = line.trim().replaceAll(" +", " ");
                if (line.equals("")) {
                    line = reader.readLine();
                    continue;
                }
                countLine++;
                String[] s = line.split(" ");

                Word first = new Word(s[0]);
                Word second = new Word(s[1]);
                RelationType relationType = null;
                switch (s[2]) {
                    case "def": {
                        relationType = RelationType.DEF;
                        break;
                    }
                    case "ass": {
                        relationType = RelationType.ASS;
                        break;
                    }
                    case "syn": {
                        relationType = RelationType.SYN;
                        break;
                    }
                    default: {
                        throw new Exception("Unknown type of relations!");
                    }
                }

                dict.addPair(first, second, relationType);
                // считываем остальные строки в цикле
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dict;
    }


}
