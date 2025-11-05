import graph.scc.SCCFinder;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPaths;
import graph.dagsp.CriticalPathResult;
import util.GraphLoader;
import util.Metrics;
import util.GraphGenerator;

import java.util.*;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        String dataPath = args.length > 0 ? args[0] : "data/sample_weighted.json";

        if (!Path.of(dataPath).toFile().exists()) {
            System.out.println("Data file not found: " + dataPath + " — generating examples in /data/");
            GraphGenerator.generateGraphs();
            dataPath = "data/small_1.json";
        }

        Map<Integer, List<int[]>> weighted = GraphLoader.loadWeightedGraph(dataPath);
        if (weighted == null || weighted.isEmpty()) {
            System.err.println("Failed to load graph or file is empty.");
            return;
        }

        Map<Integer, List<Integer>> unweighted = toUnweighted(weighted);

        Metrics sccMetrics = new Metrics();
        sccMetrics.start();
        SCCFinder sccFinder = new SCCFinder(unweighted);
        List<List<Integer>> sccs = sccFinder.findSCCs();
        sccMetrics.stop();

        System.out.println("Found " + sccs.size() + " SCC(s):");
        for (int i = 0; i < sccs.size(); i++) {
            List<Integer> comp = sccs.get(i);
            System.out.println("  comp " + i + " : " + comp + " (size=" + comp.size() + ")");
        }
        sccMetrics.print();

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

        System.out.println("\nCondensation DAG (weighted edges):");
        for (var entry : condDagWeighted.entrySet()) {
            System.out.print("  " + entry.getKey() + " -> ");
            List<String> outs = new ArrayList<>();
            for (int[] e : entry.getValue()) outs.add(e[0] + "(w=" + e[1] + ")");
            System.out.println(outs);
        }

        Map<Integer, List<Integer>> condDagUnweighted = new HashMap<>();
        for (var entry : condDagWeighted.entrySet()) {
            List<Integer> outs = new ArrayList<>();
            for (int[] e : entry.getValue()) outs.add(e[0]);
            condDagUnweighted.put(entry.getKey(), outs);
        }

        TopologicalSort topo = new TopologicalSort();
        List<Integer> topoOrder = topo.sort(condDagUnweighted);

        System.out.println("\nTopological order of components: " + topoOrder);

        DAGShortestPaths dagsp = new DAGShortestPaths(condDagWeighted);

        int sourceOriginal = 0;
        int sourceComp = compId.getOrDefault(sourceOriginal, 0);

        Metrics spMetrics = new Metrics();
        spMetrics.start();
        Map<Integer, Integer> shortest = dagsp.shortestPaths(sourceComp, topoOrder);
        spMetrics.stop();

        Metrics lpMetrics = new Metrics();
        lpMetrics.start();
        Map<Integer, Integer> longest = dagsp.longestPaths(sourceComp, topoOrder);
        lpMetrics.stop();

        System.out.println("\nShortest distances from comp " + sourceComp + ":");
        for (var entry : shortest.entrySet()) {
            System.out.println("  to " + entry.getKey() + " = " + (entry.getValue() == Integer.MAX_VALUE ? "INF" : entry.getValue()));
        }

        System.out.println("\nLongest distances from comp " + sourceComp + ":");
        for (var entry : longest.entrySet()) {
            System.out.println("  to " + entry.getKey() + " = " + (entry.getValue() == Integer.MIN_VALUE ? "N/A" : entry.getValue()));
        }

        spMetrics.print();
        lpMetrics.print();

        int targetComp = pickAnyReachable(shortest);
        if (targetComp >= 0 && shortest.get(targetComp) != Integer.MAX_VALUE) {
            List<Integer> reconstructedPath = dagsp.reconstructShortestPath(shortest, sourceComp, targetComp);
            System.out.println("\nAn example shortest path from comp " + sourceComp + " to comp " + targetComp + " : " + reconstructedPath);
            System.out.println("Path length = " + shortest.get(targetComp));
        } else {
            System.out.println("\nNo reachable target found for reconstruction.");
        }

        System.out.println("\n=== CRITICAL PATH ANALYSIS ===");

        Metrics criticalPathMetrics = new Metrics();
        criticalPathMetrics.start();
        CriticalPathResult criticalPath = dagsp.findCriticalPath(topoOrder);
        criticalPathMetrics.stop();

        System.out.println("Critical Path: " + criticalPath.path());
        System.out.println("Critical Path Length: " + criticalPath.length());

        if (criticalPath.length() > 0) {
            int endComp = criticalPath.path().get(criticalPath.path().size() - 1);
            CriticalPathResult specificCriticalPath = dagsp.findCriticalPath(sourceComp, endComp, topoOrder);
            System.out.println("Critical Path from " + sourceComp + " to " + endComp + ": " + specificCriticalPath.path());
        }

        criticalPathMetrics.print();

        System.out.println("\n=== FINAL REPORT ===");
        int nodeCount = weighted.size();
        int edgeCount = countEdges(weighted);
        boolean hasCycles = hasCycles(sccs);

        System.out.println("Graph Statistics:");
        System.out.println("  Nodes: " + nodeCount);
        System.out.println("  Edges: " + edgeCount);
        System.out.println("  Has cycles: " + hasCycles);
        System.out.println("  SCC count: " + sccs.size());

        System.out.println("\nPerformance Metrics:");
        System.out.printf("  SCC Detection: %d ms%n", sccMetrics.getElapsedTimeMs());
        System.out.printf("  Shortest Paths: %d ms%n", spMetrics.getElapsedTimeMs());
        System.out.printf("  Longest Paths: %d ms%n", lpMetrics.getElapsedTimeMs());
        System.out.printf("  Critical Path: %d ms%n", criticalPathMetrics.getElapsedTimeMs());
        System.out.printf("  Critical Path Length: %d%n", criticalPath.length());

        System.out.println("\n--- DONE ---");
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

    private static int pickAnyReachable(Map<Integer, Integer> shortest) {
        for (var e : shortest.entrySet()) {
            if (e.getValue() != Integer.MAX_VALUE) return e.getKey();
        }
        return -1;
    }

    private static int countEdges(Map<Integer, List<int[]>> graph) {
        int count = 0;
        for (List<int[]> edges : graph.values()) {
            count += edges.size();
        }
        return count;
    }

    private static boolean hasCycles(List<List<Integer>> sccs) {
        for (List<Integer> scc : sccs) {
            if (scc.size() > 1) {
                return true;
            }
        }
        return false;
    }

}