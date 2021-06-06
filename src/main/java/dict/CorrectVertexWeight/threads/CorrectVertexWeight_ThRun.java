package dict.CorrectVertexWeight.threads;

import dict.CorrectVertexWeight.CorrectVertexWeight;
import dict.DictBase;
import dict.Vertex;

import java.util.function.Function;

public class CorrectVertexWeight_ThRun implements Runnable{
    private final DictBase dictBase;
    private final CorrectVertexWeight_ThPojo correctVertexWeightThPojo;
    private final CorrectVertexWeight correctVertexWeight;
    private final int radius;
    private final double gamma;
    private final Function<Double, Double> gammaFunction;

    public CorrectVertexWeight_ThRun(CorrectVertexWeight_ThPojo correctVertexWeightThPojo, CorrectVertexWeight correctVertexWeight, DictBase dictBase, int radius, double gamma, Function<Double, Double> gammaFunction) {
        this.correctVertexWeightThPojo = correctVertexWeightThPojo;
        this.correctVertexWeight = correctVertexWeight;
        this.radius = radius;
        this.dictBase = dictBase;
        this.gamma = gamma;
        this.gammaFunction = gammaFunction;
    }

    @Override
    public void run() {
        double weightAdd = 0.0;
        for (Vertex v : correctVertexWeightThPojo.vertexList) {
            weightAdd = v.getWeight();
            correctVertexWeightThPojo.cycle.add(v);
            correctVertexWeight.funWeight(dictBase, v, weightAdd, radius, gamma, gammaFunction, correctVertexWeightThPojo.tmpWeight, correctVertexWeightThPojo.cycle);
            correctVertexWeightThPojo.cycle.remove(v);
        }
        //        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : invertMap.entrySet()) {
//            Vertex v = vertexEdgeMapEntry.getKey();
//            weightAdd = v.getWeight();
//            cycle.add(v);
//            funWeight(v, weightAdd, radius, gamma, gammaFunction, tmpWeight, cycle);
//            cycle.remove(v);
//            System.out.println((++counter) + "/" + invertMap.size());
//        }
    }
}
