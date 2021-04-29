package dict;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import mystem.StopWords;
import threads.Th;
import threads.ThRun;
import utils.Bigram;
import utils.Unigram;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

@Getter
@Setter
public class DictBase implements Serializable {

    public Map<Word, Vertex> vertex_cash = new HashMap<>();
//    static {
//        vertex_cash = new HashMap<>();
//    }

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

    @JsonIgnore
    public Set<Map.Entry<Vertex, EdgeMap>> getAllVertex() {
        return invertMap.entrySet();
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
        Vertex f = Vertex.getVertex(this, first);
        Vertex s = Vertex.getVertex(this, second);
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
     * @param w      вершина словаря
     * @param radius радиус
     * @return подсловарь
     */
    public DictBase getSubDict(Vertex w, int radius) {
        DictBase dictBase = new DictBase();
        this.getSubDict_aroundVertex(w, radius, dictBase);
        return dictBase;
    }

    public DictBase getInvertSubDict(Vertex w, int radius) {
        DictBase dictBase = new DictBase();
        this.getInvertSubDict_aroundVertex(w, radius, dictBase);
        return dictBase;
    }

    /**
     * Получить подсловарь
     *
     * @param w        центр словаря
     * @param radius   радиус словаря (количество дуг)
     * @param dictBase (пустой словарь в котором будет результат)
     */
    private void getSubDict_aroundVertex(Vertex w, int radius, DictBase dictBase) {
        if (radius < 0)
            return;

        EdgeMap edgeMap = map.get(w);
        if (edgeMap == null)
            return;

        for (Vertex s : edgeMap.getEdgeMap().keySet()) {
            Edge edge = edgeMap.getEdgeMap().get(s);
            dictBase.addPair(w, s, edge);
            getSubDict_aroundVertex(s, radius - 1, dictBase);
        }
    }

    private void getInvertSubDict_aroundVertex(Vertex w, int radius, DictBase dictBase) {
        if (radius < 0)
            return;

        EdgeMap edgeMap = invertMap.get(w);
        if (edgeMap == null)
            return;

        for (Vertex s : edgeMap.getEdgeMap().keySet()) {
            Edge edge = edgeMap.getEdgeMap().get(s);
            dictBase.addPair(s, w, edge);
            getInvertSubDict_aroundVertex(s, radius - 1, dictBase);
        }
    }
/*
    @Deprecated
    public DictBase getSubDict(DictBase result, Vertex w, int radius, Map<Vertex, Integer> used) {
        this.getSubDict_aroundVertex(w, radius, result, used);
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
    private void getSubDict_aroundVertex(Vertex w, int radius, DictBase result, Map<Vertex, Integer> used) {
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

    public static DictBase createFromDicts(DictBase... d) {
        DictBase result = new DictBase();
        for (DictBase dictBase : d) {
            result.addSubDict(dictBase);
        }
        return result;
    }

    public void printSortedEdge(String path) throws IOException {
        List<Edge> tmp = new ArrayList<>();

        for (Map.Entry<Vertex, EdgeMap> v : this.map.entrySet()) {
            for (Map.Entry<Vertex, Edge> vertexEdgeEntry : v.getValue().getEdgeMap().entrySet()) {
                Edge edge = vertexEdgeEntry.getValue();
                tmp.add(edge);
            }
        }

        tmp = tmp.stream()
                .sorted((o1, o2) -> -Double.compare(o1.getWeight(), o2.getWeight()))
                .collect(Collectors.toList());

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");


        int maxLenght =
                tmp.stream().mapToInt(vertex -> vertex.getFrom().getWord().getStr().length() + vertex.getTo().getWord().getStr().length())
                        .max().orElseThrow(NoSuchElementException::new);

        int maxWeightLenght =
                tmp.stream().mapToInt(vertex -> decimalFormat.format(vertex.getWeight()).length())
                        .max().orElseThrow(NoSuchElementException::new);

//        for (Edge edge : tmp) {
//            writer.write(edge.getFrom().getWord().getStr() + "          " +
//                    edge.getTo().getWord().getStr() +
//                    new String(new char[maxLenght - edge.getFrom().getWord().getStr().length() -
//                            edge.getTo().getWord().getStr().length() + 10 +
//                            (maxWeightLenght - decimalFormat.format(edge.getWeight()).length())])
//                            .replace('\0', ' ') +
//                    decimalFormat.format(edge.getWeight()) + "\n");
//        }
        for (Edge edge : tmp) {
            writer.write(edge.getFrom().getWord().getStr() + "\t" +
                    edge.getTo().getWord().getStr() + "\t" +
//                    new String(new char[maxLenght - edge.getFrom().getWord().getStr().length() -
//                            edge.getTo().getWord().getStr().length() + 10 +
//                            (maxWeightLenght - decimalFormat.format(edge.getWeight()).length())])
//                            .replace('\0', ' ') +
                    decimalFormat.format(edge.getWeight()) + "\n");
        }
        writer.close();
    }

    public void printSortedVertex(String path) throws IOException {
        List<Vertex> tmp = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : this.invertMap.entrySet()) {
            tmp.add(vertexEdgeMapEntry.getKey());
        }
        tmp = tmp.stream().sorted((o1, o2) -> -Double.compare(o1.getWeight(), o2.getWeight()))
                .collect(Collectors.toList());

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        int maxLenght =
                tmp.stream().mapToInt(vertex -> vertex.getWord().getStr().length()).max().orElseThrow(NoSuchElementException::new);

        int maxWeightLenght =
                tmp.stream().mapToInt(vertex -> decimalFormat.format(vertex.getWeight()).length())
                        .max().orElseThrow(NoSuchElementException::new);

        for (Vertex vertex : tmp) {
            writer.write(vertex.getWord().getPartOfSpeech() + "\t" + vertex.getWord().getStr() + "\t" +
                    //new String(new char[maxLenght - vertex.getWord().getStr().length() + 10 +
                    //(maxWeightLenght - decimalFormat.format(vertex.getWeight()).length())])
                    //.replace('\0', ' ') +
                    decimalFormat.format(vertex.getWeight()) + "\n");
        }
        writer.close();
    }

    public void printSortedVertex(String path, PartOfSpeech partOfSpeech, int topX) throws IOException {
        List<Vertex> tmp = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : this.invertMap.entrySet()) {
            tmp.add(vertexEdgeMapEntry.getKey());
        }
        tmp = tmp.stream().sorted((o1, o2) -> -Double.compare(o1.getWeight(), o2.getWeight()))
                .collect(Collectors.toList());

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");

        int maxLenght =
                tmp.stream().mapToInt(vertex -> vertex.getWord().getStr().length()).max().orElseThrow(NoSuchElementException::new);

        int maxWeightLenght =
                tmp.stream().mapToInt(vertex -> decimalFormat.format(vertex.getWeight()).length())
                        .max().orElseThrow(NoSuchElementException::new);

        int count = 0;
        for (Vertex vertex : tmp) {
            if (PartOfSpeech.NOUN == vertex.getWord().getPartOfSpeech()) {
                writer.write(vertex.getWord().getPartOfSpeech() + "\t" + vertex.getWord().getStr() + "\t" +
                        //new String(new char[maxLenght - vertex.getWord().getStr().length() + 10 +
                        //        (maxWeightLenght - decimalFormat.format(vertex.getWeight()).length())])
                        //        .replace('\0', ' ') +
                        decimalFormat.format(vertex.getWeight()) + "\n");
            }
            count++;
            if (count > topX)
                break;
        }
        writer.close();
    }


    public void removeStopWords() throws DictException {
        StopWords stopWords = StopWords.getInstance();
        List<Vertex> deletingList = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            if (stopWords.contains(key.getWord().getStr()))
                deletingList.add(key);
        }
        for (Vertex vertex : deletingList) {
            this.deleteVertex(vertex);
        }
    }

    /**
     * Делаем связь заданного типа не ориентированной (т.е. теперь она направлена в обе стороны)
     *
     * @param relationType тип связи, которую делаем двунаправленной
     */
    public void bidirectional(RelationType relationType) {
        class TMP {
            public final Vertex from;
            public final Vertex to;
            public final double weight;
            public final RelationType relationType;

            public TMP(Vertex from, Vertex to, double weight, RelationType relationType) {
                this.from = from;
                this.to = to;
                this.weight = weight;
                this.relationType = relationType;
            }
        }
        List<TMP> list = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : this.map.entrySet()) {
            Vertex from = vertexEdgeMapEntry.getKey();
            EdgeMap value = vertexEdgeMapEntry.getValue();
            for (Map.Entry<Vertex, Edge> toMap : value.getEdgeMap().entrySet()) {
                Vertex to = toMap.getKey();
                Edge edge = toMap.getValue();
                if (relationType.equals(edge.getRelationType())) {
                    // Связь ассоциация, добавим ее в обратную сторону
                    double weight = edge.getWeight();
                    list.add(new TMP(to, from, weight, edge.getRelationType()));
                }
            }
        }
        for (TMP tmp : list) {
            this.addPair(tmp.from, tmp.to, tmp.weight, tmp.relationType);
        }
    }

    public static class FindPathHelper {
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
                double wayWeight = f.edge.getWeight();

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
     * @param first  вершина от который ищем путь
     * @param last   вершина до которой ищем путь
     * @param radius максимальный размер пути
     * @return наилучший путь между двумя вершинами, с максимальным весом
     */
    public Way findMaxWay(Vertex first, Vertex last, int radius) {
        List<Way> ways = this.findWays(first, last, radius);

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
        Long startTime = System.currentTimeMillis();
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
        System.out.println("\t\t\tdone for " + (System.currentTimeMillis() - startTime) + " ms.");

    }

    /* распространение веса вершины на ее соседей в радиусе R
     * 0,95	0,9025	0,857375	0,81450625	0,773780938
     *
     * @param radius     радиус
     * @param gamma коэффициент затухания
     * @param gamma_degree  [1,3]
     */
    public void correctVertexWeight(int radius, double gamma, int gamma_degree, boolean thread) throws DictException, InterruptedException {
        Function<Double, Double> function = null;
        switch (gamma_degree) {
            case 1: {
                function = aDouble -> aDouble;
                break;
            }
            case 2: {
                function = aDouble -> aDouble * aDouble;
                break;
            }
            case 3: {
                function = aDouble -> aDouble * aDouble * aDouble;
                break;
            }
            default: {
                throw new DictException("the passed parameter 'gamma_degree = " + gamma_degree +
                        "' is out of bounds of allowed values [1,3]");
            }
        }
        long startTime = System.currentTimeMillis();
        if (thread) {
            System.out.print("correctVertexWeightThread ...\t\t");
            correctVertexWeightThread(radius, gamma, function);
        } else {
            System.out.print("correctVertexWeight ...\t\t");
            correctVertexWeight(radius, gamma, function);
        }
        System.out.println("done for " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    private void correctVertexWeight(int radius, double gamma, Function<Double, Double> gammaFunction) throws InterruptedException {
        double weightAdd = 0;
        int counter = 0;
        List<Vertex> cycle = new ArrayList<>();            // Список для отслеживания циклов в распространении весов вершин
        Map<Vertex, Double> tmpWeight = new HashMap<>();


        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            Vertex v = vertexEdgeMapEntry.getKey();
            weightAdd = v.getWeight();
            cycle.add(v);
            funWeight(v, weightAdd, radius, gamma, gammaFunction, tmpWeight, cycle);
            cycle.remove(v);
            System.out.println((++counter) + "/" + invertMap.size());
        }

        List<Pair<Vertex, Double>> collect = tmpWeight.entrySet().stream()
                .map(v -> new Pair<>(v.getKey(), v.getValue()))
                .sorted((o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()))
                .collect(Collectors.toList());
        collect.forEach(v -> {
            System.out.println(v.getKey().getWord().getStr() + "\t\t" + v.getValue());
        });
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            Vertex v = vertexEdgeMapEntry.getKey();
            Double aDouble = tmpWeight.get(v);
            if (aDouble == null)
                aDouble = 0.0;
            try {
                v.setWeight(v.getWeight() + aDouble);
            } catch (NullPointerException e) {
                System.out.println("throw  e;");
            }
        }
    }

    private void correctVertexWeightThread(int radius, double gamma, Function<Double, Double> gammaFunction) throws InterruptedException {
        double weightAdd = 0;
        int counter = 0;

        List<Th> threads = new ArrayList<>();
        int countThreads = 4;
        int numThread = 0;
        // создаем треды
        for (int i = 0; i < countThreads; i++) {
            Th th = new Th();
            th.thread = new Thread(new ThRun(th, this, radius, gamma, gammaFunction));
            threads.add(th);
        }
        // помещаем в них обрабатываемые вершины
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            threads.get(numThread).vertexList.add(vertexEdgeMapEntry.getKey());
            numThread++;
            if (numThread == countThreads)
                numThread = 0;
        }

        for (Th thread : threads) {
            thread.thread.start();
        }
        for (Th thread : threads) {
            thread.thread.join();
        }

        Map<Vertex, Double> tmpWeight = new HashMap<>();
        for (Th th : threads) {
            for (Map.Entry<Vertex, Double> vertexDoubleEntry : th.tmpWeight.entrySet()) {
                Vertex v = vertexDoubleEntry.getKey();
                Double w = vertexDoubleEntry.getValue();
                tmpWeight.put(v, tmpWeight.get(v) == null ? w : tmpWeight.get(v) + w);
            }
        }

        System.out.print("");


//        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
//            Vertex v = vertexEdgeMapEntry.getKey();
//            weightAdd = v.getWeight();
//            cycle.add(v);
//            funWeight(v, weightAdd, radius, gamma, gammaFunction, tmpWeight, cycle);
//            cycle.remove(v);
//            System.out.println((++counter) + "/" + invertMap.size());
//        }

        List<Pair<Vertex, Double>> collect = tmpWeight.entrySet().stream()
                .map(v -> new Pair<>(v.getKey(), v.getValue()))
                .sorted((o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()))
                .collect(Collectors.toList());
//        collect.forEach(v -> {
//            System.out.println(v.getKey().getWord().getStr() + "\t\t" + v.getValue());
//        });
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
            Vertex v = vertexEdgeMapEntry.getKey();
            Double aDouble = tmpWeight.get(v);
            if (aDouble == null)
                aDouble = 0.0;
            try {
                v.setWeight(v.getWeight() + aDouble);
            } catch (NullPointerException e) {
                System.out.println("throw  e;");
            }
        }
    }

    public void funWeight(Vertex vertex, double weightAdd, int radius, double gamma, Function<Double, Double> gammaFunction,
                          Map<Vertex, Double> tmpWeight, List<Vertex> cycle) {
        if (radius < 0)
            return;
        if (weightAdd <= 0)
            return;
        // добавляем вершине вес (пока что в массиве тмп, иначе будет лавинообразное добавление)
        Double tmpW = tmpWeight.get(vertex);
        if (tmpW == null)
            tmpW = 0.0;
        tmpWeight.put(vertex, tmpW + weightAdd);

        // Все соседи текущей вершины
        EdgeMap edgeMap = map.get(vertex);
        if (edgeMap == null)
            return;

        for (Map.Entry<Vertex, Edge> vertexEdgeEntry : edgeMap.getEdgeMap().entrySet()) {
            Vertex sosed = vertexEdgeEntry.getKey();
            Edge edge = vertexEdgeEntry.getValue();
            if (!cycle.contains(sosed)) {
                cycle.add(sosed);
                funWeight(sosed, weightAdd * gamma * edge.getWeight(), radius - 1,
                        gammaFunction.apply(gamma), gammaFunction, tmpWeight, cycle
                );
                cycle.remove(sosed);
            }
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
    private void funcEdgeWeightCorrection(Vertex first, Vertex second, int radius, double betta) throws DictException {
        if (betta < 1) {
            throw new DictException(" betta should be more than 1.0 ");
        }
        final double eps = 0.05;        // минимально рассматриваемый вес пути
        final double maxLink = 0.95;    // максимально допустимый вес дуги
        Way way = findMaxWay(first, second, radius);
        try {
            if (way != null && !way.isEmpty() && way.getWeight() > eps) {
                for (Edge edge : way.getWay()) {
                    edge.setWeight(Math.min(maxLink, edge.getWeight() * betta));
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
        Long startTime = System.currentTimeMillis();
        System.out.print("correctEdgeWeight...");

        Integer maxH = bigramFrequensy.entrySet().stream()
                .max((first, second) -> first.getValue() > second.getValue() ? 1 : -1).get().getValue();

        for (Map.Entry<Bigram, Integer> one : bigramFrequensy.entrySet()) {
            Bigram bigram = one.getKey();
            Integer h = one.getValue();
            if (h > treshold) {
                double betta = (double) h / (double) maxH + 1;
                this.funcEdgeWeightCorrection(
                        Vertex.getVertex(this, bigram.getFirst()),
                        Vertex.getVertex(this, bigram.getSecond()),
                        radius,
                        betta
                );
            }
        }
        System.out.println("\t\t\tdone for " + (System.currentTimeMillis() - startTime) + " ms.");
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

        dictBase.setFlagTrain(training);

        List<Vertex> deletingVertex = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> d : dictBase.invertMap.entrySet()) {
            Vertex key = d.getKey();
            if ("кулинария".equals(key.getWord().getStr()))
                System.out.print("");
            Boolean found = findTrainInRadius(dictBase, key, R);
            Boolean foundInInvert = findTrainInInvertRadius(dictBase, key, R);
            if (!(found || foundInInvert)) {            // TODO
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
    public void setFlagTrain(DictBase train) {
        Set<Vertex> trainVertexSet = new HashSet<>(train.invertMap.keySet());

        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : this.invertMap.entrySet()) {
            Vertex dictVertex = vertexEdgeMapEntry.getKey();
            if (trainVertexSet.contains(dictVertex)) {
                dictVertex.setFlag_train(true);
            } else {
                dictVertex.setFlag_train(false);
            }
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
            System.out.println("edgeMap == null");
        // Все вершины входящие в текущую
        boolean result = false;
        for (Map.Entry<Vertex, Edge> vertexEdgeEntry : edgeMap.getEdgeMap().entrySet()) {
            Vertex v = vertexEdgeEntry.getKey();
            if (vertex.isFlag_train())
                return true;
            if (v == null)
                System.out.println("v == null");
            result = findTrainInRadius(dictBase, v, R - 1);
            if (result)
                return true;
        }
        return false;
    }

    private static Boolean findTrainInInvertRadius(DictBase dictBase, Vertex vertex, Integer R) {
        if (R == 0)
            return false;
        EdgeMap edgeMap = dictBase.map.get(vertex);
        if (edgeMap == null) {
            System.out.println("edgeMap == null");
            return false;
        }
        // Все вершины входящие в текущую
        boolean result = false;
        for (Map.Entry<Vertex, Edge> vertexEdgeEntry : edgeMap.getEdgeMap().entrySet()) {
            Vertex v = vertexEdgeEntry.getKey();
            if (vertex.isFlag_train())
                return true;
            if (v == null)
                System.out.println("v == null");
            result = findTrainInInvertRadius(dictBase, v, R - 1);
            if (result)
                return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////  ПОДСЧЕТ СУММЫ СОСЕДНИХ ИСХОДЯЩИХ ИЗ ВЕРШИНЫ ВЕРШИН  ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Для каждой вершины расчитываем сумму весов исходящих из нее вершин
     */
    public void calculateWeightOfOutgoingVertex() {
        Map<Vertex, Double> tmpMap = new HashMap<>();
        for (Map.Entry<Vertex, EdgeMap> elem : this.getAllVertex()) {
            Vertex vertex = elem.getKey();

            // Смотрим все исходящие вершины
            EdgeMap outPutEdgeMap = map.get(vertex);
            if (outPutEdgeMap != null) {
                //double w = vertex.getWeight() * A;
                //tmpMap.put(vertex, tmpMap.get(vertex) == null ? w : w + tmpMap.get(vertex));

                for (Map.Entry<Vertex, Edge> outPutElem : outPutEdgeMap.getEdgeMap().entrySet()) {
                    Vertex vertex_2 = outPutElem.getKey();
                    Edge edge = outPutElem.getValue();

                    double mov = vertex_2.getWeight();
                    tmpMap.put(vertex, tmpMap.get(vertex) == null ? mov : mov + tmpMap.get(vertex));
                    vertex.setWeightOutgoingVertex(vertex.getWeightOutgoingVertex() + vertex_2.getWeight());
                }
                //tmpMap.put(vertex, tmpMap.get(vertex) == null ? 0.0 : tmpMap.get(vertex) / outPutEdgeMap.getEdgeMap().entrySet().size());
                //vertex.setWeightOutgoingVertex(tmpMap.get(vertex));
            }
            // Смотрим все входящие вершины
           /* EdgeMap outPutEdgeMap = invertMap.get(vertex);
            if (outPutEdgeMap != null) {
                for (Map.Entry<Vertex, Edge> outPutElem : outPutEdgeMap.getEdgeMap().entrySet()) {
                    Vertex vertex_2 = outPutElem.getKey();
                    Edge edge = outPutElem.getValue();

                    double mov = vertex_2.getWeight();
                    tmpMap.put(vertex, tmpMap.get(vertex) == null ? mov : mov + tmpMap.get(vertex));
                }
                //tmpMap.put(vertex, tmpMap.get(vertex) == null ? 0.0 : tmpMap.get(vertex) / outPutEdgeMap.getEdgeMap().entrySet().size());
            }*/
        }
        List<Pair<Vertex, Double>> collect = tmpMap.entrySet().stream()
                .map(v -> new Pair<Vertex, Double>(v.getKey(), v.getValue()))
                .sorted((o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()))
                .collect(Collectors.toList());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////  КЛАСТЕРИЗАЦИЯ  /////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Функция кластеризации
     *
     * @param A коэффициент веса вершины
     * @param B коэффициент умножаемый на сумму весов соседних вершин
     * @return список объектов, в которых указаны порядки сортировки по весу вершины, по сумме весов вершин
     */
    public List<ClusterHelper> clastering(double A, double B) {
        List<ClusterHelper> sortedWeight = this.getSortedWeight();
        //Map<Vertex, ClusterHelper> mapWeight = new HashMap<>();
        //sortedWeight.forEach(cl -> mapWeight.put(cl.getVertex(), cl));

        List<ClusterHelper> sortedWeightOutgoing = this.getSortedWeightOutgoing();
        //Map<Vertex, ClusterHelper> mapWeightOutgoing = new HashMap<>();
        //sortedWeightOutgoing.forEach(cl -> mapWeightOutgoing.put(cl.getVertex(), cl));

        double topWeight = sortedWeight.get(150).getVertex().getWeight();
        double topWeightOutgoing = sortedWeightOutgoing.get(150).getVertex().getWeightOutgoingVertex();

        List<ClusterHelper> result = new ArrayList<>();
        for (ClusterHelper clusterHelper : sortedWeight) {
            Vertex vertex = clusterHelper.getVertex();
            if ("интерьер".equals(vertex.getWord().getStr()))
                System.out.print("");
            if (topWeight > vertex.getWeight())
                continue;
            result.add(clusterHelper);
            clusterHelper.setClusterWeight(vertex.getWeight() * A + vertex.getWeightOutgoingVertex() * B);
        }
        result.sort((o1, o2) -> -Double.compare(o1.getClusterWeight(), o2.getClusterWeight()));
        return result;
    }


    /**
     * Сортируем вершины по их весам в порядке убывания
     *
     * @return список вершин, начиная с вершины с наибольшим весом
     */
    private List<ClusterHelper> getSortedWeight() {
        List<ClusterHelper> list = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : this.invertMap.entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            list.add(new ClusterHelper(key));
        }
        AtomicInteger num = new AtomicInteger();
        List<ClusterHelper> list_weightSorted = list.stream()
                .sorted((o1, o2) -> -Double.compare(o1.getVertex().getWeight(), o2.getVertex().getWeight()))
                .collect(Collectors.toList());

        list_weightSorted.forEach(clusterHelper -> clusterHelper.setPlace_weight(num.getAndIncrement()));
        return list_weightSorted;
    }

    /**
     * Сортируем вершины по сумме весов их соседей в порядке убывания
     *
     * @return список вершин, начиная с вершины с наибольшей суммой весов соседних вершин
     */
    private List<ClusterHelper> getSortedWeightOutgoing() {
        List<ClusterHelper> list = new ArrayList<>();
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : this.invertMap.entrySet()) {
            Vertex key = vertexEdgeMapEntry.getKey();
            list.add(new ClusterHelper(key));
        }
        AtomicInteger num = new AtomicInteger();
        List<ClusterHelper> list_weightOutgoingSorted = list.stream()
                .sorted((o1, o2) -> -Double.compare(o1.getVertex().getWeightOutgoingVertex(), o2.getVertex().getWeightOutgoingVertex()))
                .collect(Collectors.toList());
        list_weightOutgoingSorted.forEach(clusterHelper -> clusterHelper.setPlace_weight(num.getAndIncrement()));
        System.out.print("");

        return list_weightOutgoingSorted;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////  ЧТЕНИЕ\ЗАПИСЬ СЛОВАРЯ  ///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void saveAs(String path) throws IOException {
        System.out.print("save dictionary to file (path='" + path + "') ... \t\t\t");
        Long startTime = System.currentTimeMillis();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("result\\person.dat"))) {
            oos.writeObject(this);
            System.out.println("done for " + (System.currentTimeMillis() - startTime) + " ms.");
        } catch (Exception ex) {
            System.out.print("ERROR!");
            throw ex;
        }
    }

    public static DictBase readFrom(String path) throws IOException, ClassNotFoundException {
        System.out.print("readFrom dictionary from file (path='" + path + "') ... \t\t\t");
        Long startTime = System.currentTimeMillis();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("result\\person.dat"))) {
            DictBase dictBase = (DictBase) ois.readObject();
            System.out.println("done for " + (System.currentTimeMillis() - startTime) + " ms.");
            return dictBase;
        } catch (Exception ex) {
            System.out.print("ERROR!");
            throw ex;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////  ОТРИСОВКА  ////////////////////////////////////////////////////////
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
        Graphviz.fromGraph(g).totalMemory(1000000000).height(3000).render(Format.PNG).toFile(new File(fileName));
    }

    public static void graphviz_drawHight(List<Node> graphViz, String fileName) throws IOException {
        Graph g = graph("example1").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("arial"))
                .linkAttr().with("class", "link-class")
                .with(
                        graphViz
                );
        Graphviz.fromGraph(g).totalMemory(1000000000).height(22000).render(Format.PNG).toFile(new File(fileName));
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
