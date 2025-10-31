import graph.scc.SCCFinder;
import graph.topo.TopologicalSort;
import graph.dagsp.DAGShortestPaths;
import util.GraphLoader;
import util.Metrics;
import util.GraphGenerator;

import java.util.*;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        String dataPath = args.length > 0 ? args[0] : "data/sample_weighted.json";

        if (!Path.of(dataPath).toFile().exists()) {
            System.out.println("Data file not found: " + dataPath + " — генерируем примеры в /data/");
            GraphGenerator.generateGraphs();
            dataPath = "data/small_1.json";
        }

        Map<Integer, List<int[]>> weighted = GraphLoader.loadWeightedGraph(dataPath);
        if (weighted == null || weighted.isEmpty()) {
            System.err.println("Не удалось загрузить граф или файл пустой.");
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

        Map<Integer, Map<Integer, Integer>> tmpEdgeMin = new HashMap<>(); // keep min weight if multiple edges
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
            List<Integer> reconstructedPath = reconstructShortestPath(condDagWeighted, topoOrder, sourceComp, targetComp);
            System.out.println("\nAn example shortest path from comp " + sourceComp + " to comp " + targetComp + " : " + reconstructedPath);
            System.out.println("Path length = " + shortest.get(targetComp));
        } else {
            System.out.println("\nNo reachable target found for reconstruction.");
        }

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


    private static List<Integer> reconstructShortestPath(Map<Integer, List<int[]>> dag, List<Integer> topoOrder, int source, int target) {
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> pred = new HashMap<>();
        for (int v : dag.keySet()) {
            dist.put(v, Integer.MAX_VALUE);
            pred.put(v, -1);
        }
        dist.put(source, 0);

        for (int u : topoOrder) {
            if (dist.get(u) == Integer.MAX_VALUE) continue;
            for (int[] e : dag.getOrDefault(u, List.of())) {
                int v = e[0], w = e[1];
                if (dist.get(v) > dist.get(u) + w) {
                    dist.put(v, dist.get(u) + w);
                    pred.put(v, u);
                }
            }
        }

        if (dist.get(target) == Integer.MAX_VALUE) return List.of();

        LinkedList<Integer> path = new LinkedList<>();
        int cur = target;
        while (cur != -1) {
            path.addFirst(cur);
            if (cur == source) break;
            cur = pred.get(cur);
        }
        return path;
    }
}
