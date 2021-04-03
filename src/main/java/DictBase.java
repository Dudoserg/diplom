import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

@Getter
@Setter
public class DictBase {
    private Map<Vertex, EdgeMap> map;

    private Map<Vertex, EdgeMap> invertMap;

    public DictBase(Map<Vertex, EdgeMap> map) {
        this.map = map;
        invertMap = new HashMap<>();
    }


    /**
     * Добавить ребро в граф
     * @param first первая вершина
     * @param second вторая вершина
     * @param edge дуга
     */
    public void addPair(Vertex first, Vertex second, Edge edge) {
        EdgeMap edgeMap = this.map.get(first);
        if (edgeMap == null) {
            edgeMap = new EdgeMap();
            this.map.put(first, edgeMap);
        }
        edgeMap.addEdge(second, edge);

        // invertMap для быстрого поиска листов
        EdgeMap invertEdgeMap = this.invertMap.get(second);
        if(invertEdgeMap == null){
            invertEdgeMap = new EdgeMap();
            this.invertMap.put(second, invertEdgeMap);
        }
        invertEdgeMap.addEdge(first, edge);

    }

    /**
     * Удаляем вершину из графа
     * @param vertex вершину которую необходимо удалить
     */
    public void deleteVertex(Vertex vertex){
        // афиша
//        афиша объявление def
//        афиша спектакль ass
//        афиша концерт   ass
//        афиша лекция    ass
//        афиша кинофильм ass
        EdgeMap removedMap = this.map.remove(vertex);

        if(removedMap != null && !removedMap.isEmpty()){
            // удаляем в инвертированном словаре данные связи
            // removedMap(объявление, спектакль, концерт, лекция, кинофильм )
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : removedMap.getEdgeMap().entrySet()) {
                Vertex v = vertexEdgeEntry.getKey();    // объявление
                Edge e = vertexEdgeEntry.getValue();    // ... афиша ...

                // обратный словарь
                EdgeMap edgeMap = invertMap.get(v);
                Edge invertRemovedMap = edgeMap.getEdgeMap().remove(vertex);
            }
        }

        //// по инвертированному списку пробуем найти вершины-листы, чтобы удалить связи до них
        EdgeMap invertEdgeMap = invertMap.get(vertex);
        if(invertEdgeMap != null && !invertEdgeMap.isEmpty()){
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : invertEdgeMap.getEdgeMap().entrySet()) {
                Vertex invertKey = vertexEdgeEntry.getKey();        // от этой вершины идет дука к удаляемое
                Edge invertValue = vertexEdgeEntry.getValue();

                EdgeMap mapWhereNeedToRemove = this.map.get(invertKey);
                mapWhereNeedToRemove.getEdgeMap().remove(vertex);
            }
            invertMap.remove(vertex);
        }
    }




    /**
     * Получить подСловарь
     *
     * @param w вершина словаря
     * @param r радиус
     * @return подсловарь
     */
    public DictBase getSubDict(Vertex w, int r) {
        DictBase dictBase = new DictBase(new HashMap<>());
        this.getSubDict(w, r, dictBase);
        return dictBase;
    }

    /**
     * Получить подсловарь
     * @param w центр словаря
     * @param r радиус словаря (количество дуг)
     * @param dictBase (пустой словарь в котором будет результат)
     */
    private void getSubDict(Vertex w, int r, DictBase dictBase) {
        if (r < 0)
            return;

        EdgeMap edgeMap = map.get(w);
        if (edgeMap == null)
            return;

        for (Vertex s : edgeMap.getEdgeMap().keySet()) {
            Edge edge = edgeMap.getEdgeMap().get(s);
            dictBase.addPair(w, s, edge);
            getSubDict(s, r - 1, dictBase);
        }
    }

    /**
     * Добавить в текущий слоловарь подсловарь
     * @param dictBase добавляемый словарь
     */
    public void addSubDict(DictBase dictBase){
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.map.entrySet()) {
            Vertex first_vertex = vertexEdgeMapEntry.getKey();
            EdgeMap edgeMap = vertexEdgeMapEntry.getValue();
            for (Map.Entry<Vertex, Edge> secondMap : edgeMap.getEdgeMap().entrySet()) {
                Vertex second_vertex = secondMap.getKey();
                Edge edge = secondMap.getValue();
                this.addPair(first_vertex, second_vertex, edge);
            }
        }
    }




    /**
     * # Получить ноды для отрисовки графа в графвизе
     * @param map словарь который будем рисовать
     * @return  Список нод для библиотеки графвиз
     * @throws DictException    такого типа отношений не существует
     */
    public static List<Node> getGraphViz( Map<Vertex, EdgeMap> map) throws DictException {
        List<Node> result = new ArrayList<>();
        Set<Map.Entry<Vertex, EdgeMap>> entries = map.entrySet();

        for (Map.Entry<Vertex, EdgeMap> entry : entries) {
            Vertex first = entry.getKey();
            EdgeMap value = entry.getValue();

            for (Map.Entry<Vertex, Edge> wordRelationTypeEntry : value.getEdgeMap().entrySet()) {
                Vertex second = wordRelationTypeEntry.getKey();
                Edge edge = wordRelationTypeEntry.getValue();
                RelationType type = edge.getRelationType();

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
                        throw new DictException("UNKNOWN RELATIONS TYPE");
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
