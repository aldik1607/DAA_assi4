import graph.scc.SCCFinder;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPaths;
import graph.dagsp.CriticalPathResult;
import util.GraphLoader;
import util.Metrics;

import java.util.*;
import java.nio.file.*;

public class RunAll {
    public static void main(String[] args) {
        String[] datasets = {
                "data/small_1.json", "data/small_2.json", "data/small_3.json",
                "data/medium_1.json", "data/medium_2.json", "data/medium_3.json",
                "data/large_1.json", "data/large_2.json", "data/large_3.json"
        };

        System.out.println("=== RUNNING ALL DATASETS ===\n");

        for (String dataPath : datasets) {
            if (!Files.exists(Path.of(dataPath))) {
                System.out.println(" Missing: " + dataPath);
                continue;
            }

            System.out.println(dataPath);
            runDataset(dataPath);
            System.out.println("──────────────────────────────");
        }
    }

    private static void runDataset(String dataPath) {
        try {
            Map<Integer, List<int[]>> weighted = GraphLoader.loadWeightedGraph(dataPath);
            if (weighted == null || weighted.isEmpty()) return;

            Map<Integer, List<Integer>> unweighted = toUnweighted(weighted);

            // SCC
            Metrics sccMetrics = new Metrics();
            sccMetrics.start();
            SCCFinder sccFinder = new SCCFinder(unweighted);
            List<List<Integer>> sccs = sccFinder.findSCCs();
            sccMetrics.stop();

            // Condensation
            Map<Integer, Integer> compId = new HashMap<>();
            for (int i = 0; i < sccs.size(); i++) {
                for (int node : sccs.get(i)) compId.put(node, i);
            }

            Map<Integer, Map<Integer, Integer>> tmpEdgeMin = new HashMap<>();
            for (int u : weighted.keySet()) {
                for (int[] edge : weighted.getOrDefault(u, List.of())) {
                    int v = edge[0], w = edge[1];
                    int cu = compId.get(u), cv = compId.get(v);
                    if (cu != cv) {
                        tmpEdgeMin.putIfAbsent(cu, new HashMap<>());
                        Map<Integer, Integer> row = tmpEdgeMin.get(cu);
                        row.put(cv, Math.min(row.getOrDefault(cv, Integer.MAX_VALUE), w));
                    }
                }
            }

            Map<Integer, List<int[]>> condDagWeighted = new HashMap<>();
            for (int i = 0; i < sccs.size(); i++) condDagWeighted.put(i, new ArrayList<>());
            for (var e : tmpEdgeMin.entrySet()) {
                int from = e.getKey();
                for (var toEntry : e.getValue().entrySet()) {
                    int to = toEntry.getKey();
                    int w = toEntry.getValue();
                    condDagWeighted.get(from).add(new int[]{to, w});
                }
            }

            Map<Integer, List<Integer>> condDagUnweighted = new HashMap<>();
            for (var entry : condDagWeighted.entrySet()) {
                List<Integer> outs = new ArrayList<>();
                for (int[] e : entry.getValue()) outs.add(e[0]);
                condDagUnweighted.put(entry.getKey(), outs);
            }

            // Topological Sort
            TopologicalSort topo = new TopologicalSort();
            List<Integer> topoOrder = topo.sort(condDagUnweighted);

            // Shortest/Longest Paths
            DAGShortestPaths dagsp = new DAGShortestPaths(condDagWeighted);
            int sourceComp = 0;

            Metrics spMetrics = new Metrics();
            spMetrics.start();
            Map<Integer, Integer> shortest = dagsp.shortestPaths(sourceComp, topoOrder);
            spMetrics.stop();

            Metrics lpMetrics = new Metrics();
            lpMetrics.start();
            Map<Integer, Integer> longest = dagsp.longestPaths(sourceComp, topoOrder);
            lpMetrics.stop();

            // Critical Path
            Metrics cpMetrics = new Metrics();
            cpMetrics.start();
            CriticalPathResult criticalPath = dagsp.findCriticalPath(topoOrder);
            cpMetrics.stop();

            System.out.printf("Nodes: %d, Edges: %d, SCCs: %d%n",
                    weighted.size(), countEdges(weighted), sccs.size());
            System.out.printf("Times: SCC=%dms, SP=%dms, LP=%dms%n",
                    sccMetrics.getElapsedTimeMs(), spMetrics.getElapsedTimeMs(), lpMetrics.getElapsedTimeMs());
            System.out.printf("Critical Path: %d (length)%n", criticalPath.length());

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static Map<Integer, List<Integer>> toUnweighted(Map<Integer, List<int[]>> weighted) {
        Map<Integer, List<Integer>> out = new HashMap<>();
        for (var entry : weighted.entrySet()) {
            List<Integer> list = new ArrayList<>();
            for (int[] e : entry.getValue()) list.add(e[0]);
            out.put(entry.getKey(), list);
        }
        return out;
    }

    private static int countEdges(Map<Integer, List<int[]>> graph) {
        int count = 0;
        for (List<int[]> edges : graph.values()) count += edges.size();
        return count;
    }
}