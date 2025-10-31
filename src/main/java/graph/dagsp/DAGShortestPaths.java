package graph.dagsp;

import java.util.*;

public class DAGShortestPaths {
    private Map<Integer, List<int[]>> dag; // node -> list of {to, weight}

    public DAGShortestPaths(Map<Integer, List<int[]>> dag) {
        this.dag = dag;
    }

    public Map<Integer, Integer> shortestPaths(int source, List<Integer> topoOrder) {
        Map<Integer, Integer> dist = new HashMap<>();
        for (int node : dag.keySet()) dist.put(node, Integer.MAX_VALUE);
        dist.put(source, 0);

        for (int u : topoOrder) {
            if (dist.get(u) != Integer.MAX_VALUE) {
                for (int[] edge : dag.getOrDefault(u, List.of())) {
                    int v = edge[0], w = edge[1];
                    if (dist.get(v) > dist.get(u) + w)
                        dist.put(v, dist.get(u) + w);
                }
            }
        }
        return dist;
    }

    public Map<Integer, Integer> longestPaths(int source, List<Integer> topoOrder) {
        Map<Integer, Integer> dist = new HashMap<>();
        for (int node : dag.keySet()) dist.put(node, Integer.MIN_VALUE);
        dist.put(source, 0);

        for (int u : topoOrder) {
            if (dist.get(u) != Integer.MIN_VALUE) {
                for (int[] edge : dag.getOrDefault(u, List.of())) {
                    int v = edge[0], w = edge[1];
                    if (dist.get(v) < dist.get(u) + w)
                        dist.put(v, dist.get(u) + w);
                }
            }
        }
        return dist;
    }
}
