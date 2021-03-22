import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

public class Dict {
    private Map<Word, Edges> map;

    public Dict(Map<Word, Edges> map) {
        this.map = map;
    }

    public Map<Word, Edges> getMap() {
        return map;
    }

    public void setMap(Map<Word, Edges> map) {
        this.map = map;
    }

    public void addPair(Word first, Word second, RelationType relationType) {
        Edges edges = this.map.get(first);
        if (edges == null) {
            edges = new Edges();
            this.map.put(first, edges);
        }
        edges.addWord(second, relationType);
    }


    /**
     * Получить подСловарь
     *
     * @param w вершина словаря
     * @param r радиус
     * @return
     */
    public Dict getSubDict(Word w, int r) {
        Dict dict = new Dict(new HashMap<>());
        this.getSubDict(w, r, dict);
        return dict;
    }


    private void getSubDict(Word w, int r, Dict dict) {
        if (r < 0)
            return;

        Edges edges = map.get(w);
        if (edges == null)
            return;

        for (Word s : edges.getEdgeMap().keySet()) {
            RelationType relationType = edges.getEdgeMap().get(s);
            dict.addPair(w, s, relationType);
            getSubDict(s, r - 1, dict);
        }
    }


    /**
     * Получить ноды для отрисовки графа в графвизе
     *
     * @return
     * @throws Exception
     */
    public List<Node> getGraphViz() throws Exception {
        List<Node> result = new ArrayList<>();
        Set<Map.Entry<Word, Edges>> entries = this.map.entrySet();

        for (Map.Entry<Word, Edges> entry : entries) {
            Word first = entry.getKey();
            Edges value = entry.getValue();

            for (Map.Entry<Word, RelationType> wordRelationTypeEntry : value.getEdgeMap().entrySet()) {
                Word second = wordRelationTypeEntry.getKey();
                RelationType type = wordRelationTypeEntry.getValue();

                Node link1 = node(first.getWord()).with(Color.BLACK);
                Node link2 = node(second.getWord()).with(Color.BLACK);
                Node resultNode = null;

                switch (type) {
                    case ASS: {
                        resultNode = link1.link(
                                to(link2).with(Color.BLACK, Label.of("ass"))
                        );
                        break;
                    }
                    case SYN: {
                        resultNode = link1.link(
                                to(link2).with(Color.RED, Label.of("syn"))
                        );
                        break;
                    }
                    case DEF: {
                        resultNode = link1.link(
                                to(link2).with(Color.GREEN, Label.of("def"))
                        );
                        break;
                    }
                    default: {
                        throw new Exception("UNKNOWN RELATIONS TYPE");
                    }
                }
                result.add(resultNode);
            }
        }
        return result;
    }


    public static void draw(List<Node> graphViz, String fileName) throws IOException {
        Graph g = graph("example1").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("arial"))
                .linkAttr().with("class", "link-class")
                .with(
                        graphViz
                );
        Graphviz.fromGraph(g).height(1200).render(Format.PNG).toFile(new File(fileName));
    }
}
