import dict.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.node;

public class Start {
    public Start() throws DictException, IOException {
        DictBase dictBase = readDictFromFile();

        //DictBase.draw(DictBase.getGraphViz(dictBase.getMap()), "example/map.png");
        //DictBase.draw(DictBase.getGraphViz(dictBase.getInvertMap()), "example/invert.png");

        //dictBase.deleteVertex(new Vertex("объявление"));

        //DictBase.draw(DictBase.getGraphViz(dictBase.getMap()), "example/map_after.png");
        //DictBase.draw(DictBase.getGraphViz(dictBase.getInvertMap()), "example/invert_after.png");

        List<DictBase.FindPathHelper> path = new ArrayList<>();

        List<Vertex> anyWay = dictBase.findAnyWay(new Vertex("0"), new Vertex("8"), 10);

        List<List<Vertex>> ways = dictBase.findWays(new Vertex("0"), new Vertex("8"), 10);

        for (List<Vertex> way : ways) {
            System.out.println(way.stream().map(vertex -> vertex.getWord().getStr()).collect(Collectors.joining("  ")));
        }

        System.out.print("");
//        {
//            dict.DictBase subDictBase = dictBase.getSubDict(new dict.Vertex("афиша"), 1);
//            subDictBase.addPair(new dict.Vertex("афиша"), new dict.Vertex("test"), new dict.Edge(dict.RelationType.ASS));
//            dictBase.addSubDict(subDictBase);
//            dict.DictBase.draw(dict.DictBase.getGraphViz(dictBase.getMap()), "example/subDict_after.png");
//        }

//        for (int i = 1; i < 10; i++) {
//            dict.DictBase.draw(dictBase.getSubDict(new dict.Vertex("афиша"), i).getGraphViz(), "example/subDict_" + i + ".png");
//        }

    }

    public DictBase readDictFromFile() {
        DictBase dictBase = new DictBase();
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

                Vertex first = new Vertex(s[0]);
                Vertex second = new Vertex(s[1]);
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

                dictBase.addPair(first, second, new Edge(relationType));
                // считываем остальные строки в цикле
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dictBase;
    }


}
