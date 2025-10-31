package graph.topo;

import java.util.*;

public class TopologicalSort {
    public List<Integer> sort(Map<Integer, List<Integer>> dag) {
        Map<Integer, Integer> indegree = new HashMap<>();
        for (var entry : dag.entrySet()) {
            indegree.putIfAbsent(entry.getKey(), 0);
            for (int v : entry.getValue())
                indegree.put(v, indegree.getOrDefault(v, 0) + 1);
        }

        Queue<Integer> q = new ArrayDeque<>();
        for (var e : indegree.entrySet())
            if (e.getValue() == 0) q.add(e.getKey());

        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int node = q.poll();
            order.add(node);
            for (int next : dag.getOrDefault(node, List.of())) {
                indegree.put(next, indegree.get(next) - 1);
                if (indegree.get(next) == 0) q.add(next);
            }
        }
        return order;
    }
}
