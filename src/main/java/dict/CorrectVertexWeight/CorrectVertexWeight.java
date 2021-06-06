package dict.CorrectVertexWeight;

import dict.CorrectVertexWeight.threads.CorrectVertexWeight_ThPojo;
import dict.CorrectVertexWeight.threads.CorrectVertexWeight_ThRun;
import dict.DictBase;
import dict.DictException;
import dict.Edge.Edge;
import dict.EdgeMap;
import dict.Vertex;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CorrectVertexWeight implements CorrectVertexWeightInterface {
    private final int radius;
    private final double gamma;
    private final int gamma_degree;
    private final int countThreads;

    public CorrectVertexWeight(int radius, double gamma, int gamma_degree, int countThreads) {
        this.radius = radius;
        this.gamma = gamma;
        this.gamma_degree = gamma_degree;
        this.countThreads = countThreads;
    }

    @Override
    public void correctVertexWeight(DictBase dictBase) throws DictException, InterruptedException, IOException, IllegalAccessException {
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
        if (countThreads > 0) {
            System.out.print("correctVertexWeightThread ...\t\t");
            correctVertexWeightThread(dictBase, radius, gamma, function, countThreads);
        } else {
            System.out.print("correctVertexWeight ...\t\t");
            correctVertexWeight(dictBase, radius, gamma, function);
        }
        System.out.println("done for " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    @Deprecated
    private void correctVertexWeight(DictBase dictBase, int radius, double gamma, Function<Double, Double> gammaFunction)
            throws InterruptedException {
        double weightAdd = 0;
        int counter = 0;
        List<Vertex> cycle = new ArrayList<>();            // Список для отслеживания циклов в распространении весов вершин
        Map<Vertex, Double> tmpWeight = new HashMap<>();


        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
            Vertex v = vertexEdgeMapEntry.getKey();
            weightAdd = v.getWeight();
            cycle.add(v);
            funWeight(dictBase, v, weightAdd, radius, gamma, gammaFunction, tmpWeight, cycle);
            cycle.remove(v);
            System.out.println((++counter) + "/" + dictBase.getInvertMap().size());
        }

        List<Pair<Vertex, Double>> collect = tmpWeight.entrySet().stream()
                .map(v -> new Pair<>(v.getKey(), v.getValue()))
                .sorted((o1, o2) -> -Double.compare(o1.getValue(), o2.getValue()))
                .collect(Collectors.toList());
        collect.forEach(v -> {
            System.out.println(v.getKey().getWord().getStr() + "\t\t" + v.getValue());
        });
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
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

    @Deprecated
    private void correctVertexWeightThread(DictBase dictBase, int radius, double gamma, Function<Double, Double> gammaFunction,
                                           int countThreads)
            throws InterruptedException, IOException, IllegalAccessException {
        double weightAdd = 0;
        int counter = 0;

        List<CorrectVertexWeight_ThPojo> threads = new ArrayList<>();
        int numThread = 0;
        // создаем треды
        for (int i = 0; i < countThreads; i++) {
            CorrectVertexWeight_ThPojo correctVertexWeightThPojo = new CorrectVertexWeight_ThPojo();
            correctVertexWeightThPojo.thread = new Thread(new CorrectVertexWeight_ThRun(correctVertexWeightThPojo, this, dictBase, radius, gamma, gammaFunction));
            threads.add(correctVertexWeightThPojo);
        }
        // помещаем в них обрабатываемые вершины
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
            threads.get(numThread).vertexList.add(vertexEdgeMapEntry.getKey());
            numThread++;
            if (numThread == countThreads)
                numThread = 0;
        }

        for (CorrectVertexWeight_ThPojo thread : threads) {
            thread.thread.start();
        }
        for (CorrectVertexWeight_ThPojo thread : threads) {
            thread.thread.join();
        }

        Map<Vertex, Double> tmpWeight = new HashMap<>();
        for (CorrectVertexWeight_ThPojo correctVertexWeightThPojo : threads) {
            for (Map.Entry<Vertex, Double> vertexDoubleEntry : correctVertexWeightThPojo.tmpWeight.entrySet()) {
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
        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
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

    @Deprecated
    public void funWeight(DictBase dictBase, Vertex vertex, double weightAdd, int radius, double gamma, Function<Double, Double> gammaFunction,
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
        EdgeMap edgeMap = dictBase.getMap().get(vertex);
        if (edgeMap == null)
            return;

        for (Map.Entry<Vertex, Edge> vertexEdgeEntry : edgeMap.getEdgeMap().entrySet()) {
            Vertex sosed = vertexEdgeEntry.getKey();
            Edge edge = vertexEdgeEntry.getValue();
            if (!cycle.contains(sosed)) {
                cycle.add(sosed);
                funWeight(dictBase, sosed, weightAdd * gamma * edge.getWeight(), radius - 1,
                        gammaFunction.apply(gamma), gammaFunction, tmpWeight, cycle
                );
                cycle.remove(sosed);
            }
        }
    }

}
