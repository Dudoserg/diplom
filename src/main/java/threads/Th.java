package threads;

import dict.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Th {
    public List<Vertex> cycle = new ArrayList<>();            // Список для отслеживания циклов в распространении весов вершин
    public Map<Vertex, Double> tmpWeight = new HashMap<>();
    public Thread thread;
    public List<Vertex> vertexList = new ArrayList<>();
}
