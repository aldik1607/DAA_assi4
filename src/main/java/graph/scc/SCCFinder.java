package graph.scc;

import java.util.*;

public class SCCFinder {
    private Map<Integer, List<Integer>> graph;
    private boolean[] visited;
    private int[] ids, low;
    private boolean[] onStack;
    private Deque<Integer> stack;
    private int id = 0;
    private List<List<Integer>> sccs = new ArrayList<>();

    public SCCFinder(Map<Integer, List<Integer>> graph) {
        this.graph = graph;
        int n = graph.size();
        visited = new boolean[n];
        ids = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new ArrayDeque<>();
    }

    public List<List<Integer>> findSCCs() {
        for (int i = 0; i < graph.size(); i++)
            if (!visited[i]) dfs(i);
        return sccs;
    }

    private void dfs(int at) {
        visited[at] = true;
        stack.push(at);
        onStack[at] = true;
        ids[at] = low[at] = id++;

        for (int to : graph.getOrDefault(at, Collections.emptyList())) {
            if (!visited[to]) dfs(to);
            if (onStack[to]) low[at] = Math.min(low[at], low[to]);
        }

        // Если нашли корень SCC
        if (ids[at] == low[at]) {
            List<Integer> scc = new ArrayList<>();
            while (true) {
                int node = stack.pop();
                onStack[node] = false;
                scc.add(node);
                if (node == at) break;
            }
            sccs.add(scc);
        }
    }
}
