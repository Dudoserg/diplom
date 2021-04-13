package dict;

import dict.Edge.Edge;
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
import java.text.DecimalFormat;
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

    public EdgeMap getNeighbours(Vertex v) {
        return this.map.get(v);
    }



    /**
     * Добавить дугу в граф
     *
     * @param first  первая вершина
     * @param second вторая вершина
     * @param edge   дуга
     */
    private void addPair(Vertex first, Vertex second, Edge edge) {
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

    public void addPair(String first, String second, double weight, RelationType relationType) {
        Vertex f = Vertex.getVertex(first);
        Vertex s = Vertex.getVertex(second);
        this.addPair(f, s, new Edge(f, s, weight, relationType));
    }

    public void addPair(Vertex first, Vertex second, double weight, RelationType relationType) {
        this.addPair(first, second, new Edge(first, second, weight, relationType));
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
        Edge edge;
        int prev;
        int weight;
        int index;
        Set<Vertex> used;       // для отсечения циклов

        public FindPathHelper(Vertex vertex, Edge edge, int prev, int weight, int index, Set<Vertex> used) {
            this.vertex = vertex;
            this.edge = edge;
            this.prev = prev;
            this.weight = weight;
            this.index = index;
            this.used = used;
        }
    }

    /**
     * Поиск всех путей между вершинами first, last удовлеворяющих условию
     *
     * @param first - вершина из которой ведется поиск
     * @param last  - вершина которую необходимо достигнуть
     * @param R     - максимальная длина пути, которые рассматриваем
     * @return List<List < Vertex>> список всех возможных путей между вершинами
     */
    public List<Way> findWays(Vertex first, Vertex last, int R) {
        /// извлекаем все возможные пути
        List<List<Vertex>> result = new ArrayList<>();
        List<Way> waysList = new ArrayList<>();

        List<FindPathHelper> path = new ArrayList<>();
        EdgeMap edgeMap = map.get(first);
        // первый этап
        for (Map.Entry<Vertex, Edge> variant : edgeMap.getEdgeMap().entrySet()) {
            Vertex v = variant.getKey();
            Edge e = variant.getValue();
            if (v.equals(last)) {
                // сразу нашли слово, вот это повезло(нет)
                result.add(new ArrayList<>(Arrays.asList(first, last)));

                ArrayList<Edge> edges = new ArrayList<>(Collections.singletonList(e));
                waysList.add(new Way(edges, e.getWeight(), 1));
                // TODO посчтитать веса
            } else {
                FindPathHelper findPathHelper = new FindPathHelper(v, e, -1, 1, path.size(), new HashSet<>());
                path.add(findPathHelper);
            }
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
                    FindPathHelper f = new FindPathHelper(v.getKey(), v.getValue(), prev.index, prev.weight + 1, path.size(), prev.used);
                    if (!f.used.add(v.getKey()))
                        continue;
                    path.add(f);
                }
        }


        for (FindPathHelper f : path) {
            if (f.vertex.equals(last)) {
                List<Vertex> currentPath = new ArrayList<>(Collections.singletonList(last));
                result.add(currentPath);

                List<Edge> currentPath_edge = new ArrayList<>(Collections.singletonList(f.edge));
                Double wayWeight = f.edge.getWeight();

                FindPathHelper tmp = f;
                while (tmp.prev != -1) {
                    tmp = path.get(tmp.prev);
                    currentPath.add(tmp.vertex);
                    currentPath_edge.add(tmp.edge);
                    wayWeight *= tmp.edge.getWeight();
                }
//                currentPath.add(tmp.vertex);
                currentPath.add(first);
                Collections.reverse(currentPath);


                Collections.reverse(currentPath_edge);
                waysList.add(new Way(currentPath_edge, wayWeight, currentPath_edge.size()));
            }
        }

        return waysList;
    }

    @Deprecated
    public List<Vertex> findAnyWay(Vertex first, Vertex last, int R) {
        List<FindPathHelper> path = new ArrayList<>();
        EdgeMap edgeMap = map.get(first);

        // первый этап
        for (Map.Entry<Vertex, Edge> variant : edgeMap.getEdgeMap().entrySet()) {
            Vertex v = variant.getKey();
            Edge e = variant.getValue();
            FindPathHelper findPathHelper = new FindPathHelper(v, e, -1, 1, path.size(), new HashSet<>());
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
                    FindPathHelper f = new FindPathHelper(v.getKey(), v.getValue(), prev.index, prev.weight + 1, path.size(), prev.used);
                    if (!f.used.add(v.getKey()))
                        continue;
                    path.add(f);
                    if (f.vertex.equals(last))
                        break outerloop;
                }
        }

        /// извлекаем все возможные пути
        List<Vertex> result = new ArrayList<>(Collections.singletonList(last));

        for (FindPathHelper f : path) {
            if (f.vertex.equals(last)) {
                FindPathHelper tmp = f;
                while (true) {
                    tmp = path.get(tmp.prev);
                    result.add(tmp.vertex);
                    if (tmp.prev == -1) {
                        result.add(first);
                        break;
                    }
                }
            }
        }
        Collections.reverse(result);
        return result;
    }


    /**
     * Поиск пути с максимальным весом
     *
     * @param first вершина от который ищем путь
     * @param last  вершина до которой ищем путь
     * @param R     максимальный размер пути
     * @return наилучший путь между двумя вершинами, с максимальным весом
     */
    public Way findMaxWay(Vertex first, Vertex last, int R) {
        List<Way> ways = this.findWays(first, last, R);

        Way bestWay = null;
        double bestWayWeight = 0.0;

        for (Way way : ways) {
            double tmp = 1.0;
            for (Edge edge : way.getWay()) {
                tmp *= edge.getWeight();
            }
            if (tmp > bestWayWeight) {
                bestWayWeight = tmp;
                bestWay = way;
            }
        }
        System.out.println();
        return bestWay;
    }




    /**
     * Функция корректировки весов дуг по биграмм
     *
     * @param first  первая часть биграммы
     * @param second вторая часть биграммы
     * @param betta  коэффициент усилиния веса > 1
     */
    public void funcEdgeWeightCorrection(Vertex first, Vertex second, double betta) throws DictException {
        if (betta < 1) {
            throw new DictException(" betta should be more than 1.0 ");
        }
        final double eps = 0.05;        // минимально рассматриваемый вес пути
        final double maxLink = 0.95;    // максимально допустимый вес дуги
        final int r = 5;                // радиус поиска связи между вершинами
        Way way = findMaxWay(first, second, r);
        if (!way.isEmpty()) {
            for (Edge edge : way.getWay()) {
                edge.setWeight(edge.getWeight() * betta);
            }
        } else {
            //TODO установить верный тип связи
            addPair(first, second, eps * betta, RelationType.UNKNOWN);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * # Получить ноды для отрисовки графа в графвизе
     *
     * @param dictBase словарь который будем рисовать
     * @return Список нод для библиотеки графвиз
     * @throws DictException такого типа отношений не существует
     */
    public static List<Node> getGraphViz(DictBase dictBase) throws DictException {
        Map<Vertex, EdgeMap> map = dictBase.getMap();
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
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                String weight_str = ("(" + decimalFormat.format(edge.getWeight()) + ")").replace(",", ".");
                switch (type) {
                    case ASS: {
                        resultNode = link1.link(
                                to(link2).with(Color.BLACK,Font.size(9), Label.of("ass" + weight_str))
                        );
                        break;
                    }
                    case SYN: {
                        resultNode = link1.link(
                                to(link2).with(Color.RED,Font.size(9), Label.of("syn" + weight_str))
                        );
                        break;
                    }
                    case DEF: {
                        resultNode = link1.link(
                                to(link2).with(Color.GREEN,Font.size(9), Label.of("def" + weight_str))
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

    /**
     * Отрисовать граф
     * @param graphViz Данный для отрисовки
     * @param fileName путь по которому сохраняется файл
     * @throws IOException еррорина
     */
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
