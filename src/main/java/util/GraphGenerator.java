package util;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GraphGenerator {

    private static Random random = new Random();

    public static void generateGraphs() {
        generateGraphsForSize("small", 6, 10);
        generateGraphsForSize("medium", 10, 20);
        generateGraphsForSize("large", 20, 50);
    }

    private static void generateGraphsForSize(String name, int minNodes, int maxNodes) {
        for (int i = 1; i <= 3; i++) {
            int n = random.nextInt(maxNodes - minNodes + 1) + minNodes;
            double density = (i == 1) ? 0.2 : (i == 2) ? 0.5 : 0.8;
            Map<Integer, List<int[]>> graph = generateRandomGraph(n, density, true);
            saveGraph(graph, "data/" + name + "_" + i + ".json");
        }
    }

    private static Map<Integer, List<int[]>> generateRandomGraph(int n, double density, boolean weighted) {
        Map<Integer, List<int[]>> graph = new HashMap<>();
        for (int i = 0; i < n; i++) graph.put(i, new ArrayList<>());

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && Math.random() < density) {
                    int weight = weighted ? (random.nextInt(9) + 1) : 1;
                    graph.get(i).add(new int[]{j, weight});
                }
            }
        }
        return graph;
    }

    private static void saveGraph(Map<Integer, List<int[]>> graph, String path) {
        JsonObject obj = new JsonObject();
        for (var entry : graph.entrySet()) {
            JsonArray arr = new JsonArray();
            for (int[] edge : entry.getValue()) {
                JsonObject e = new JsonObject();
                e.addProperty("to", edge[0]);
                e.addProperty("weight", edge[1]);
                arr.add(e);
            }
            obj.add(String.valueOf(entry.getKey()), arr);
        }

        try {
            Files.createDirectories(Path.of("data"));
            Files.writeString(Path.of(path), new GsonBuilder().setPrettyPrinting().create().toJson(obj));
            System.out.println("✅ Graph saved: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
