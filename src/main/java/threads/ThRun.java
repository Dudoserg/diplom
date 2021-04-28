package threads;

import dict.DictBase;
import dict.Vertex;

import java.util.function.Function;

public class ThRun implements Runnable{
    Th th;
    DictBase dictBase;
    int radius;
    double gamma;
    Function<Double, Double> gammaFunction;

    public ThRun(Th th, DictBase dictBase, int radius, double gamma, Function<Double, Double> gammaFunction) {
        this.th = th;
        this.dictBase = dictBase;
        this.radius = radius;
        this.gamma = gamma;
        this.gammaFunction = gammaFunction;
    }

    @Override
    public void run() {
        double weightAdd = 0.0;
        for (Vertex v : th.vertexList) {
            weightAdd = v.getWeight();
            th.cycle.add(v);
            dictBase.funWeight(v, weightAdd, radius, gamma, gammaFunction, th.tmpWeight, th.cycle);
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
