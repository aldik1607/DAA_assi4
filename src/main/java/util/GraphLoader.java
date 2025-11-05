package util;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GraphLoader {

    public static Map<Integer, List<Integer>> loadGraph(String path) {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        try {
            String json = Files.readString(Path.of(path));
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            for (String key : obj.keySet()) {
                int node = Integer.parseInt(key);
                JsonArray arr = obj.getAsJsonArray(key);
                List<Integer> neighbors = new ArrayList<>();
                for (JsonElement el : arr)
                    neighbors.add(el.getAsInt());
                graph.put(node, neighbors);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }

    // Загрузка графа с весами
    public static Map<Integer, List<int[]>> loadWeightedGraph(String path) {
        Map<Integer, List<int[]>> graph = new HashMap<>();
        try {
            String json = Files.readString(Path.of(path));
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            for (String key : obj.keySet()) {
                int node = Integer.parseInt(key);
                JsonArray arr = obj.getAsJsonArray(key);
                List<int[]> edges = new ArrayList<>();
                for (JsonElement el : arr) {
                    JsonObject edge = el.getAsJsonObject();
                    int to = edge.get("to").getAsInt();
                    int w = edge.get("weight").getAsInt();
                    edges.add(new int[]{to, w});
                }
                graph.put(node, edges);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }
}
