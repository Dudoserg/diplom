package dict.SetVertexWeight;

import dict.DictBase;
import dict.EdgeMap;
import dict.Vertex;
import utils.Unigram;

import java.util.Map;
import java.util.stream.Collectors;

public class SetVertexWeight implements SetVertexWeightInterface {

    private final Map<Unigram, Integer> unigramFrequensy;

    public SetVertexWeight(Map<Unigram, Integer> unigramFrequensy) {
       this.unigramFrequensy = unigramFrequensy;
    }

    @Override
    public void setVertexWeight(DictBase dictBase) {
        Long startTime = System.currentTimeMillis();
        System.out.print("setVertexWeight");

        Map<String, Integer> collect =
                unigramFrequensy.entrySet().stream()
                        .collect(Collectors.toMap(
                                o -> o.getKey().getFirst(), Map.Entry::getValue
                        ));

        for (Map.Entry<Vertex, EdgeMap> vertexEdgeMapEntry : dictBase.getInvertMap().entrySet()) {
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
}
