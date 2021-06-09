package dict.ModificateEdge;

import dict.*;
import dict.Edge.Edge;
import utils.Bigram;

import java.util.Map;

public class ModificateEdgeByBigramm implements ModificateEdgeInterface {

    private final Map<Bigram, Integer> bigramFrequensy;
    private final int treshold;
    private final int radius;

    public ModificateEdgeByBigramm(Map<Bigram, Integer> bigramFrequensy, int treshold, int radius) {
        this.bigramFrequensy = bigramFrequensy;
        this.treshold = treshold;
        this.radius = radius;
    }

    @Override
    public void modificate(DictBase dictBase) throws DictException {
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
                        dictBase,
                        dictBase.getVertex(bigram.getFirst()),
                        dictBase.getVertex(bigram.getSecond()),
                        radius,
                        betta
                );
            }
        }
        System.out.println("\t\t\tdone for " + (System.currentTimeMillis() - startTime) + " ms.");
    }

    /**
     * Функция корректировки весов дуг по биграмм
     *
     * @param first  первая часть биграммы
     * @param second вторая часть биграммы
     * @param betta  коэффициент усилиния веса > 1
     */
    private void funcEdgeWeightCorrection(DictBase dictBase, Vertex first, Vertex second, int radius, double betta) throws DictException {
        if (betta < 1) {
            throw new DictException(" betta should be more than 1.0 ");
        }
        final double eps = 0.05;        // минимально рассматриваемый вес пути
        final double maxLink = 0.95;    // максимально допустимый вес дуги
        Way way = dictBase.findMaxWay(first, second, radius);
        try {
            if (way != null && !way.isEmpty() && way.getWeight() > eps) {
                for (Edge edge : way.getWay()) {
                    edge.setWeight(Math.min(maxLink, edge.getWeight() * betta));
                }
            } else {
                //TODO установить верный тип связи
                dictBase.addPair(first, second, eps * betta, RelationType.ASS);
            }
        } catch (NullPointerException e) {
            throw e;
        }
    }

}
