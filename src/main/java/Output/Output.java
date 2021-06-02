package Output;

import dict.Cluster;
import dict.Edge.Edge;
import dict.PartOfSpeech;
import dict.RelationType;
import dict.Vertex;
import lombok.Getter;
import lombok.Setter;
import utils.Helper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Output {
    private Set<PartOfSpeech> partOfSpeechSet = new HashSet<>();

    private Map<PartOfSpeech, Integer> partOfSpeechIntegerMap = new HashMap<>();
    private Map<Integer, PartOfSpeech> integerPartOfSpeechMap = new HashMap<>();

    public void createPartOfSpeechHashMap() {
        for (PartOfSpeech partOfSpeech : this.partOfSpeechSet) {
            if (partOfSpeech == null)
                continue;
            partOfSpeechIntegerMap.put(partOfSpeech, partOfSpeech.ordinal());
            integerPartOfSpeechMap.put(partOfSpeech.ordinal(), partOfSpeech);
        }
    }

    public void savePartOfSpeech() throws IOException {
        List<String> csvVertex = new ArrayList<>();
        for (Map.Entry<Integer, PartOfSpeech> elem : integerPartOfSpeechMap.entrySet()) {
            Integer key = elem.getKey();
            PartOfSpeech value = elem.getValue();
            try {
                String str = value.getStr();
                csvVertex.add(key + ";" + str);
            } catch (NullPointerException e) {
                System.out.println();
            }

        }
        String text = csvVertex.stream().collect(Collectors.joining("\n"));
        text = "id;str\n" + text;
        Helper.saveToFile(text, Helper.path("output", "partOfSpeech.txt"));
    }

    //////////////////////////
    private Set<Vertex> vertexSet = new HashSet<>();

    private Map<Vertex, Integer> vertexIntegerMap = new HashMap<>();
    private Map<Integer, Vertex> integerVertexMap = new HashMap<>();

    public void createVertexHashMap() {
        int counter = 0;
        for (Vertex vertex : this.vertexSet) {
            vertexIntegerMap.put(vertex, counter);
            integerVertexMap.put(counter, vertex);
            counter++;
        }
    }

    public void saveVertex() throws IOException {
        List<String> csvVertex = new ArrayList<>();
        for (Map.Entry<Integer, Vertex> elem : integerVertexMap.entrySet()) {
            Integer key = elem.getKey();
            Vertex value = elem.getValue();
            try {
                String str = value.getWord().getStr();
                String partOfSpeech = value.getWord().getPartOfSpeech() != null ? String.valueOf(value.getWord().getPartOfSpeech().ordinal()) : "null";
                double weight = value.getWeight();
                int flagTrain = value.isFlag_train() ? 1 : 0;
                double weightOutgoingVertex = value.getWeightOutgoingVertex();

                csvVertex.add(
                        key + ";" +
                                str + ";" +
                                partOfSpeech + ";" +
                                +weight + ";" +
                                weightOutgoingVertex + ";" +
                                flagTrain);
            } catch (NullPointerException e) {
                System.out.println();
            }

        }
        String text = csvVertex.stream().collect(Collectors.joining("\n"));
        text = "id;str;partOfSpeech;weight;weightOutgoingVertex;flagTrain\n" + text;
        Helper.saveToFile(text, Helper.path("output", "vertex.txt"));
    }

    //////////////////////////
    private List<Edge> edgeList = new ArrayList<>();

    public void saveEdge() throws IOException {
        List<String> result = new ArrayList<>();
        int counter = 0;
        for (Edge edge : this.edgeList) {
            Vertex from = edge.getFrom();
            int fromId = this.vertexIntegerMap.get(from);

            Vertex to = edge.getTo();
            int toId = this.vertexIntegerMap.get(to);

            double weight = edge.getWeight();
            RelationType relationType = edge.getRelationType();
            result.add(
                    (counter++) + ";" +
                            fromId + ";" +
                            //from.getWord().getStr() + ";" +
                            toId + ";" +
                            //to.getWord().getStr() + ";" +
                            weight + ";" +
                            relationType.getStr()
            );
        }
        String text = result.stream().collect(Collectors.joining("\n"));
        text = "id;fromId;toId;weight;relationType\n" + text;
        Helper.saveToFile(text, Helper.path("output", "edge.txt"));
    }

    //////////////////////////
    private Set<Cluster> clusterSet = new HashSet<>();
    private Map<Cluster, Integer> clusterIntegerMap = new HashMap<>();
    private Map<Integer, Cluster> integerClusterMap = new HashMap<>();

    public void createClusterHashMap() {
        int counter = 0;
        for (Cluster cluster : this.clusterSet) {
            clusterIntegerMap.put(cluster, counter);
            integerClusterMap.put(counter, cluster);
            counter++;
        }
    }

    public void saveCluster() throws IOException {
        List<String> csvVertex = new ArrayList<>();
        for (Map.Entry<Integer, Cluster> elem : integerClusterMap.entrySet()) {
            Integer key = elem.getKey();
            Cluster value = elem.getValue();
            try {
                Vertex vertex = value.getVertex();
                int vertexId = vertexIntegerMap.get(vertex);
                double weight = value.getWeight();

                csvVertex.add(key + ";" + vertexId + ";" + weight);
            } catch (NullPointerException e) {
                System.out.println();
            }

        }
        String text = csvVertex.stream().collect(Collectors.joining("\n"));
        text = "id;vertexId;weight\n" + text;
        Helper.saveToFile(text, Helper.path("output", "cluster.txt"));
    }

    ////////////////////////// vertex cluster


    List<VertexAndCluster> vertexAndClustersList = new ArrayList<>();

    public void saveVertexAndClusters() throws IOException {
        List<String> result = new ArrayList<>();
        int counter = 0;
        for (VertexAndCluster vertexAndCluster : vertexAndClustersList) {
            Cluster cluster = vertexAndCluster.getCluster();
            Integer clusterId = clusterIntegerMap.get(cluster);

            Vertex vertex = vertexAndCluster.getVertex();
            Integer vertexId = vertexIntegerMap.get(vertex);

            int distance = vertexAndCluster.getDistance();

            result.add(
                    (counter++) + ";" +
                            clusterId + ";" +
                            vertexId + ";" +
                            distance
            );
        }
        String text = result.stream().collect(Collectors.joining("\n"));
        text = "id;clusterId;vertexId\n" + text;

        Helper.saveToFile(text, Helper.path("output", "vertexAndClusters.txt"));
    }
}
