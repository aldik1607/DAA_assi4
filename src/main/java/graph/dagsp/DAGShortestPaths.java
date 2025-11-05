package graph.dagsp;

import java.util.*;

public class DAGShortestPaths {
    private Map<Integer, List<int[]>> dag;

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


    public CriticalPathResult findCriticalPath(List<Integer> topoOrder) {
        if (topoOrder.isEmpty()) {
            return new CriticalPathResult(List.of(), 0);
        }

        int source = topoOrder.get(0);

        Map<Integer, Integer> longestDistances = longestPaths(source, topoOrder);

        int maxDistance = Integer.MIN_VALUE;
        int endNode = source;

        for (Map.Entry<Integer, Integer> entry : longestDistances.entrySet()) {
            if (entry.getValue() > maxDistance && entry.getValue() != Integer.MIN_VALUE) {
                maxDistance = entry.getValue();
                endNode = entry.getKey();
            }
        }

        List<Integer> criticalPath = reconstructLongestPath(longestDistances, source, endNode);

        return new CriticalPathResult(criticalPath, maxDistance);
    }


    public CriticalPathResult findCriticalPath(int source, int target, List<Integer> topoOrder) {
        Map<Integer, Integer> longestDistances = longestPaths(source, topoOrder);
        int length = longestDistances.getOrDefault(target, Integer.MIN_VALUE);

        if (length == Integer.MIN_VALUE) {
            return new CriticalPathResult(List.of(), 0);
        }

        List<Integer> path = reconstructLongestPath(longestDistances, source, target);
        return new CriticalPathResult(path, length);
    }


    private List<Integer> reconstructLongestPath(Map<Integer, Integer> dist, int source, int target) {
        if (dist.get(target) == Integer.MIN_VALUE) {
            return List.of();
        }

        LinkedList<Integer> path = new LinkedList<>();
        int current = target;


        while (current != source) {
            path.addFirst(current);

            boolean foundPredecessor = false;
            for (int u : dag.keySet()) {
                for (int[] edge : dag.getOrDefault(u, List.of())) {
                    int v = edge[0], w = edge[1];
                    if (v == current && dist.get(u) != Integer.MIN_VALUE &&
                            dist.get(current) == dist.get(u) + w) {
                        current = u;
                        foundPredecessor = true;
                        break;
                    }
                }
                if (foundPredecessor) break;
            }

            if (!foundPredecessor) {
                break;
            }
        }

        path.addFirst(source);
        return path;
    }


    public List<Integer> reconstructShortestPath(Map<Integer, Integer> dist, int source, int target) {
        if (dist.get(target) == Integer.MAX_VALUE) {
            return List.of();
        }

        LinkedList<Integer> path = new LinkedList<>();
        int current = target;

        while (current != source) {
            path.addFirst(current);

            boolean foundPredecessor = false;
            for (int u : dag.keySet()) {
                for (int[] edge : dag.getOrDefault(u, List.of())) {
                    int v = edge[0], w = edge[1];
                    if (v == current && dist.get(u) != Integer.MAX_VALUE &&
                            dist.get(current) == dist.get(u) + w) {
                        current = u;
                        foundPredecessor = true;
                        break;
                    }
                }
                if (foundPredecessor) break;
            }

            if (!foundPredecessor) break;
        }

        path.addFirst(source);
        return path;
    }
}