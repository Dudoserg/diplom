package dict.CorrectVertexWeight.threads;

import dict.CorrectVertexWeight.CorrectVertexWeight;
import dict.DictBase;
import dict.Vertex;

import java.util.function.Function;

public class ThRun implements Runnable{
    private final DictBase dictBase;
    private final Th th;
    private final CorrectVertexWeight correctVertexWeight;
    private final int radius;
    private final double gamma;
    private final Function<Double, Double> gammaFunction;

    public ThRun(Th th, CorrectVertexWeight correctVertexWeight, DictBase dictBase, int radius, double gamma, Function<Double, Double> gammaFunction) {
        this.th = th;
        this.correctVertexWeight = correctVertexWeight;
        this.radius = radius;
        this.dictBase = dictBase;
        this.gamma = gamma;
        this.gammaFunction = gammaFunction;
    }

    @Override
    public void run() {
        double weightAdd = 0.0;
        for (Vertex v : th.vertexList) {
            weightAdd = v.getWeight();
            th.cycle.add(v);
            correctVertexWeight.funWeight(dictBase, v, weightAdd, radius, gamma, gammaFunction, th.tmpWeight, th.cycle);
            th.cycle.remove(v);
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
