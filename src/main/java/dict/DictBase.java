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
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import utils.Bigram;
import utils.Unigram;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

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


    public Edge getEdge(Vertex f, Vertex t) {
        EdgeMap edgeMap = this.getMap().get(f);
        if (edgeMap == null) {
            return null;
        }
        Edge edge = edgeMap.getEdgeMap().get(t);
        return edge;
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
        {
            EdgeMap invertEdgeMap = this.invertMap.get(second);
            if (invertEdgeMap == null) {
                invertEdgeMap = new EdgeMap();
                this.invertMap.put(second, invertEdgeMap);
            }
            invertEdgeMap.addEdge(first, edge);
        }
        {
            EdgeMap invertEdgeMap = this.invertMap.get(first);
            if (invertEdgeMap == null) {
                invertEdgeMap = new EdgeMap();
                this.invertMap.put(first, invertEdgeMap);
            }
        }
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
     * @param vertexForDel вершину которую необходимо удалить
     */
    public void deleteVertex(Vertex vertexForDel) throws DictException {
        // афиша
//        афиша объявление def
//        афиша спектакль ass
//        афиша концерт   ass
//        афиша лекция    ass
//        афиша кинофильм ass
        EdgeMap removedMap = this.map.remove(vertexForDel);

        if (removedMap != null && !removedMap.isEmpty()) {
            // удаляем в инвертированном словаре данные связи
            // removedMap(объявление, спектакль, концерт, лекция, кинофильм )
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : removedMap.getEdgeMap().entrySet()) {
                Vertex v = vertexEdgeEntry.getKey();    // объявление
                //Edge e = vertexEdgeEntry.getValue();    // ... афиша ...

                // обратный словарь
                EdgeMap edgeMap = invertMap.get(v);
                Edge invertRemovedMap = edgeMap.getEdgeMap().remove(vertexForDel);
            }
        }

        //// по инвертированному списку пробуем найти вершины-листы, чтобы удалить связи до них
//        EdgeMap invertEdgeMap = invertMap.get(vertexForDel);
//        if (invertEdgeMap != null && !invertEdgeMap.isEmpty()) {
//            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : invertEdgeMap.getEdgeMap().entrySet()) {
//                Vertex invertKey = vertexEdgeEntry.getKey();        // от этой вершины идет дука к удаляемое
//                Edge invertValue = vertexEdgeEntry.getValue();
//
//                EdgeMap mapWhereNeedToRemove = this.map.get(invertKey);
//                mapWhereNeedToRemove.getEdgeMap().remove(vertexForDel);
//            }
//            invertMap.remove(vertexForDel);
//        }

        EdgeMap removedInvertMap = this.invertMap.remove(vertexForDel);
        if (removedInvertMap != null && !removedInvertMap.isEmpty()) {
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : removedInvertMap.getEdgeMap().entrySet()) {
                Vertex v = vertexEdgeEntry.getKey();
                Edge edge = vertexEdgeEntry.getValue();

                EdgeMap edgeMap = this.map.get(v);
                if (edgeMap != null && !edgeMap.getEdgeMap().isEmpty()) {
                    edgeMap.getEdgeMap().remove(vertexForDel);
                } else {
                    throw new DictException("neightboor is empty");
                }
            }
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
        this.getSubDict_aroundVertex(w, r, dictBase);
        return dictBase;
    }

    /**
     * Получить подсловарь
     *
     * @param w        центр словаря
     * @param r        радиус словаря (количество дуг)
     * @param dictBase (пустой словарь в котором будет результат)
     */
    private void getSubDict_aroundVertex(Vertex w, int r, DictBase dictBase) {
        if (r < 0)
            return;

        EdgeMap edgeMap = map.get(w);
        if (edgeMap == null)
            return;

        for (Vertex s : edgeMap.getEdgeMap().keySet()) {
            Edge edge = edgeMap.getEdgeMap().get(s);
            dictBase.addPair(w, s, edge);
            getSubDict_aroundVertex(s, r - 1, dictBase);
        }
    }
/*
    @Deprecated
    public DictBase getSubDict(DictBase result, Vertex w, int r, Map<Vertex, Integer> used) {
        this.getSubDict_aroundVertex(w, r, result, used);
        return result;
    }*/

    /**
     * Получить подсловарь
     *
     * @param w        центр словаря
     * @param r        радиус словаря (количество дуг)
     * @param result (пустой словарь в котором будет результат)
     * @param used (пустой словарь в котором будет результат)
     */
    /*@Deprecated
    private void getSubDict_aroundVertex(Vertex w, int r, DictBase result, Map<Vertex, Integer> used) {
        if (r < 0)
            return;

        EdgeMap edgeMap = map.get(w);
        if (edgeMap == null)
            return;

        for (Vertex s : edgeMap.getEdgeMap().keySet()) {
            Integer integer = used.get(s);
            if (integer != null && r < integer) {
                continue;
            } else {
                Edge edge = edgeMap.getEdgeMap().get(s);
                result.addPair(w, s, edge);
                if (used.get(s) == null) {
                    used.put(s, r);
                } else {
                    if (r > used.get(s)) {
                        used.put(s, r);
                    }
                }
                getSubDict_aroundVertex(s, r - 1, result, used);
            }

        }
    }*/


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
        if (edgeMap == null) {
            return waysList;
        }
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
        //System.out.println();
        return bestWay;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////    КОРРЕКТИРОВКА ВЕСОВ ВЕРШИН     ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setVertexWeight(Map<Unigram, Integer> unigramFrequensy) {
        System.out.print("setVertexWeight");

        Map<String, Integer> collect =
                unigramFrequensy.entrySet().stream()
                        .collect(Collectors.toMap(
                                o -> o.getKey().getFirst(), Map.Entry::getValue
                        ));

        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            Integer integer = collect.get(key.getWord().getStr());
            if (integer != null) {
                key.setWeight(integer.doubleValue());
            } else {
                key.setWeight(0.0);
            }
        }
        System.out.println("\t\t\tdone");

    }

    /**
     * распространение веса вершины на ее соседей в радиусе R
     *  0,95	0,9025	0,857375	0,81450625	0,773780938
     *
     * @param r         радиус
     * @param gamma     коэффициент затухания
     */
    public void correctVertexWeight( int r, double gamma) {
        System.out.print("correctVertexWeight...");

        Map<Vertex, Double> tmpWeight = new HashMap<>();
        double weightAdd = 0;
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            Vertex v = vertexEdgeMapEntry.getKey();
            weightAdd = v.getWeight();
            funWeight(v, weightAdd, r, gamma, tmpWeight);
        }
        List<Pair<Vertex, Double>> collect = tmpWeight.entrySet().stream()
                .map(v -> new Pair<>(v.getKey(), v.getValue()))
                .sorted((o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()))
                .collect(Collectors.toList());
        collect.forEach(v->{
            System.out.println(v.getKey().getWord().getStr() + "\t\t" + v.getValue());
        });
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            Vertex v = vertexEdgeMapEntry.getKey();
            Double aDouble = tmpWeight.get(v);
            v.setWeight(v.getWeight() + aDouble);
        }
        System.out.println("\t\t\tdone");
    }

    private void funWeight(Vertex vertex, double weightAdd, int r, double gamma, Map<Vertex, Double> tmpWeight) {
        if (r < 0)
            return;
        if (weightAdd <= 0)
            return;

        // добавляем вершине вес (пока что в массиве тмп, иначе будет лавинообразное добавление)
        Double tmpW = tmpWeight.get(vertex);
        if (tmpW == null)
            tmpW = 0.0;
        tmpWeight.put(vertex, tmpW + weightAdd);


        EdgeMap edgeMap = map.get(vertex);
        if (edgeMap == null)
            return;

        for (Map.Entry<Vertex, Edge> vertexEdgeEntry : edgeMap.getEdgeMap().entrySet()) {
            Vertex sosed = vertexEdgeEntry.getKey();
            Edge edge = vertexEdgeEntry.getValue();
            funWeight(sosed, weightAdd * gamma * edge.getWeight(), r - 1, gamma * gamma, tmpWeight);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////    КОРРЕКТИРОВКА ВЕСОВ ДУГ //////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Функция корректировки весов дуг по биграмм
     *
     * @param first  первая часть биграммы
     * @param second вторая часть биграммы
     * @param betta  коэффициент усилиния веса > 1
     */
    private void funcEdgeWeightCorrection(Vertex first, Vertex second, int r, double betta) throws DictException {
        if (betta < 1) {
            throw new DictException(" betta should be more than 1.0 ");
        }
        final double eps = 0.05;        // минимально рассматриваемый вес пути
        final double maxLink = 0.95;    // максимально допустимый вес дуги
        Way way = findMaxWay(first, second, r);
        try {
            if (way != null && !way.isEmpty() && way.getWeight() > eps) {
                for (Edge edge : way.getWay()) {
                    edge.setWeight(edge.getWeight() * betta);
                }
            } else {
                //TODO установить верный тип связи
                addPair(first, second, eps * betta, RelationType.ASS);
            }
        } catch (NullPointerException e) {
            throw e;
        }
    }

    public void correctEdgeWeight(Map<Bigram, Integer> bigramFrequensy, int treshold, int radius) throws DictException {
        System.out.print("correctEdgeWeight...");

        Integer maxH = bigramFrequensy.entrySet().stream()
                .max((first, second) -> first.getValue() > second.getValue() ? 1 : -1).get().getValue();

        for (Map.Entry<Bigram, Integer> one : bigramFrequensy.entrySet()) {
            Bigram bigram = one.getKey();
            Integer h = one.getValue();
            if (h > treshold) {
                double betta = (double) h / (double) maxH + 1;
                this.funcEdgeWeightCorrection(Vertex.getVertex(bigram.getFirst()), Vertex.getVertex(bigram.getSecond()), radius, betta);
            }
        }
        System.out.println("\t\t\tdone");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////    УДАЛЕНИЕ ЛИШНИХ СЛОВ ИЗ БАЗОВОГО ГРАФА  ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Из всего графа, выделяем только те слова, что находятся в обучающей выборке + слова в радиусе R
     *
     * @param training
     * @return
     */
    public static void removeUnusedVertex(DictBase dictBase, DictBase training, int R) throws DictException {
        System.out.print("removeUnusedVertex...");

        training.setFlagTrain();

        List<Vertex> deletingVertex = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> d : dictBase.invertMap.entrySet()) {
            Vertex key = d.getKey();
            Boolean found = findTrainInRadius(dictBase, key, R);
            if (!found) {
                deletingVertex.add(key);
            }
        }

        for (int i = deletingVertex.size() - 1; i >= 0; i--) {
            dictBase.deleteVertex(deletingVertex.get(i));
        }
        //System.out.println();
        // Выбираем из базового словаря все слова из тренировочной, + все слова в радиусе 5 для каждой вершины из тренировочной выборки
//        int x = 0;
//        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : training.getMap().entrySet()) {
//            Vertex vertexFrom_training = vertexEdgeMapEntry.getKey();
//
//            Map<Vertex, Integer> tmpMap = new HashMap<>();
//            base.getSubDict(result, vertexFrom_training, R, tmpMap);
//            //result.addSubDict(subDict);
//            System.out.println(x++ + "/" + training.getMap().size());
//        }
        System.out.println("\t\t\tdone");
    }

    /**
     * Помечаем все вершины как тренировочные
     */
    public void setFlagTrain() {
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : this.invertMap.entrySet()) {
            vertexEdgeMapEntry.getKey().setFlag_train(true);
        }
    }

    /**
     * Проверяем, входит ли вершина в радиус тренировочной выборки
     *
     * @param vertex вершина, которую проверяем, входит ли она в радиус Р тренировочной выборки
     * @param R      радиус
     * @return да\нет
     */
    private static Boolean findTrainInRadius(DictBase dictBase, Vertex vertex, Integer R) {
        if (R == 0)
            return false;
        EdgeMap edgeMap = dictBase.invertMap.get(vertex);
        if (edgeMap == null)
            System.out.println();
        // Все вершины входящие в текущую
        boolean result = false;
        for (Map.Entry<Vertex, Edge> vertexEdgeEntry : edgeMap.getEdgeMap().entrySet()) {
            Vertex v = vertexEdgeEntry.getKey();
            if (vertex.isFlag_train())
                return true;
            if (v == null)
                System.out.println();
            result = findTrainInRadius(dictBase, v, R - 1);
            if (result)
                return true;
        }
        return false;
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
    public static List<Node> graphviz_getGraphViz(DictBase dictBase) throws DictException {
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
                                to(link2).with(Color.BLACK, Font.size(9), Label.of("ass" + weight_str))
                        );
                        break;
                    }
                    case SYN: {
                        resultNode = link1.link(
                                to(link2).with(Color.RED, Font.size(9), Label.of("syn" + weight_str))
                        );
                        break;
                    }
                    case DEF: {
                        resultNode = link1.link(
                                to(link2).with(Color.GREEN, Font.size(9), Label.of("def" + weight_str))
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
     *
     * @param graphViz Данный для отрисовки
     * @param fileName путь по которому сохраняется файл
     * @throws IOException еррорина
     */
    public static void graphviz_draw(List<Node> graphViz, String fileName) throws IOException {
        Graph g = graph("example1").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("arial"))
                .linkAttr().with("class", "link-class")
                .with(
                        graphViz
                );
        Graphviz.fromGraph(g).totalMemory(1000000000).render(Format.PNG).toFile(new File(fileName));
    }

    public static void graphviz_graphSaveToFile(List<Node> graphViz, String fileName, Format format) throws IOException {
        Graph g = graph("example1").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("arial"))
                .linkAttr().with("class", "link-class")
                .with(
                        graphViz
                );
        Graphviz.fromGraph(g).totalMemory(1000000000).render(format).toFile(new File(fileName));
    }
}
