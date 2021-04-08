package dict;

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

    public DictBase() {
        this.map = new HashMap<>();
        invertMap = new HashMap<>();
    }


    public void addPair(String first, String second, Edge edge) {
        this.addPair(new Vertex(first), new Vertex(second), edge);
    }

    /**
     * Добавить ребро в граф
     *
     * @param first  первая вершина
     * @param second вторая вершина
     * @param edge   дуга
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
        if (invertEdgeMap == null) {
            invertEdgeMap = new EdgeMap();
            this.invertMap.put(second, invertEdgeMap);
        }
        invertEdgeMap.addEdge(first, edge);

    }

    /**
     * Удаляем вершину из графа
     *
     * @param vertex вершину которую необходимо удалить
     */
    public void deleteVertex(Vertex vertex) {
        // афиша
//        афиша объявление def
//        афиша спектакль ass
//        афиша концерт   ass
//        афиша лекция    ass
//        афиша кинофильм ass
        EdgeMap removedMap = this.map.remove(vertex);

        if (removedMap != null && !removedMap.isEmpty()) {
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
        if (invertEdgeMap != null && !invertEdgeMap.isEmpty()) {
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
        DictBase dictBase = new DictBase();
        this.getSubDict(w, r, dictBase);
        return dictBase;
    }

    /**
     * Получить подсловарь
     *
     * @param w        центр словаря
     * @param r        радиус словаря (количество дуг)
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
     *
     * @param dictBase добавляемый словарь
     */
    public void addSubDict(DictBase dictBase) {
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

    public class FindPathHelper {
        Vertex vertex;
        int prev;
        int weight;
        int index;
        Set<Vertex> used;

        public FindPathHelper(Vertex vertex, int prev, int weight, int index, Set<Vertex> used) {
            this.vertex = vertex;
            this.prev = prev;
            this.weight = weight;
            this.index = index;
            this.used = used;
        }
    }

    /**
     * Поиск путей между вершинами first, last
     *
     * @param first - вершина из которой ведется поиск
     * @param last  - вершина которую необходимо достигнуть
     * @param R     - максимальная длина пути, которые рассматриваем
     * @return List<List < Vertex>> список всех возможных путей между вершинами
     */
    public List<List<Vertex>> findWays(Vertex first, Vertex last, int R) {
        List<FindPathHelper> path = new ArrayList<>();
        EdgeMap edgeMap = map.get(first);
        // первый этап
        for (Map.Entry<Vertex, Edge> variant : edgeMap.getEdgeMap().entrySet()) {
            Vertex v = variant.getKey();
            FindPathHelper findPathHelper = new FindPathHelper(v, -1, 1, path.size(), new HashSet<>());
            path.add(findPathHelper);
        }
        int maxR = 0;
        int index = 0;
        while (true) {
            FindPathHelper prev = null;
            if (path.size() <= index)
                break;
            prev = path.get(index++);

            maxR = Math.max(maxR, prev.weight);
            if (maxR > R - 1)
                break;

            edgeMap = map.get(prev.vertex);
            if (edgeMap != null)
                for (Map.Entry<Vertex, Edge> v : edgeMap.getEdgeMap().entrySet()) {
                    FindPathHelper f = new FindPathHelper(v.getKey(), prev.index, prev.weight + 1, path.size(), prev.used);
                    if (!f.used.add(v.getKey()))
                        continue;
                    path.add(f);
                }
        }
        /// извлекаем все возможные пути
        List<List<Vertex>> result = new ArrayList<>();

        for (FindPathHelper f : path) {
            if (f.vertex.equals(last)) {
                List<Vertex> currentPath = new ArrayList<>(Collections.singletonList(last));
                result.add(currentPath);
                FindPathHelper tmp = f;
                while (true) {
                    tmp = path.get(tmp.prev);
                    currentPath.add(tmp.vertex);
                    if (tmp.prev == -1) {
                        currentPath.add(first);
                        break;
                    }
                }
                Collections.reverse(currentPath);
            }
        }
        return result;
    }

    public List<Vertex> findAnyWay(Vertex first, Vertex last, int R) {
        List<FindPathHelper> path = new ArrayList<>();
        EdgeMap edgeMap = map.get(first);

        // первый этап
        for (Map.Entry<Vertex, Edge> variant : edgeMap.getEdgeMap().entrySet()) {
            Vertex v = variant.getKey();
            FindPathHelper findPathHelper = new FindPathHelper(v, -1, 1, path.size(), new HashSet<>());
            path.add(findPathHelper);
        }
        int maxR = 0;
        int index = 0;

        outerloop:
        while (true) {
            FindPathHelper prev = null;
            if (path.size() <= index)
                break;
            prev = path.get(index++);

            maxR = Math.max(maxR, prev.weight);
            if (maxR > R - 1)
                break;

            edgeMap = map.get(prev.vertex);
            if (edgeMap != null)
                for (Map.Entry<Vertex, Edge> v : edgeMap.getEdgeMap().entrySet()) {
                    FindPathHelper f = new FindPathHelper(v.getKey(), prev.index, prev.weight + 1, path.size(), prev.used);
                    if (!f.used.add(v.getKey()))
                        continue;
                    path.add(f);
                    if(f.vertex.equals(last))
                        break outerloop;
                }
        }

        /// извлекаем все возможные пути
        List<Vertex> result = new ArrayList<>(Collections.singletonList(first));

        for (FindPathHelper f : path) {
            if (f.vertex.equals(last)) {
                FindPathHelper tmp = f;
                while (true) {
                    tmp = path.get(tmp.prev);
                    result.add(tmp.vertex);
                    if (tmp.prev == -1) {
                        result.add(last);
                        break;
                    }
                }
            }
        }

        return result;
    }

    public void funLink_2(Vertex first, Vertex second, double betta) {
        final double eps = 0.05;        // минимально рассматриваемый вес пути
        final double maxLink = 0.95;    // максимально допустимый вес дуги
        final int r = 5;                // радиус поиска связи между вершинами
//        if()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * # Получить ноды для отрисовки графа в графвизе
     *
     * @param map словарь который будем рисовать
     * @return Список нод для библиотеки графвиз
     * @throws DictException такого типа отношений не существует
     */
    public static List<Node> getGraphViz(Map<Vertex, EdgeMap> map) throws DictException {
        List<Node> result = new ArrayList<>();
        Set<Map.Entry<Vertex, EdgeMap>> entries = map.entrySet();

        for (Map.Entry<Vertex, EdgeMap> entry : entries) {
            Vertex first = entry.getKey();
            EdgeMap value = entry.getValue();

            for (Map.Entry<Vertex, Edge> wordRelationTypeEntry : value.getEdgeMap().entrySet()) {
                Vertex second = wordRelationTypeEntry.getKey();
                Edge edge = wordRelationTypeEntry.getValue();
                RelationType type = edge.getRelationType();

                Node link1 = node(first.getWord().getStr()).with(Color.BLACK);
                Node link2 = node(second.getWord().getStr()).with(Color.BLACK);
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
