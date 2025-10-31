package graph.dagsp;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DAGShortestPathsTest {

    @Test
    public void shortestAndLongestSimple() {
        // Weighted DAG:
        // 0 -> 1 (w=2), 0->2 (w=4)
        // 1 -> 2 (w=1), 1->3 (w=7)
        // 2 -> 3 (w=3)
        Map<Integer, List<int[]>> dag = new HashMap<>();
        dag.put(0, new ArrayList<>(List.of(new int[]{1,2}, new int[]{2,4})));
        dag.put(1, new ArrayList<>(List.of(new int[]{2,1}, new int[]{3,7})));
        dag.put(2, new ArrayList<>(List.of(new int[]{3,3})));
        dag.put(3, new ArrayList<>());

        // Topological order (one possible): 0,1,2,3
        List<Integer> topo = List.of(0,1,2,3);

        DAGShortestPaths dsp = new DAGShortestPaths(dag);

        Map<Integer, Integer> shortest = dsp.shortestPaths(0, topo);
        // Expected shortest distances: 0->0=0, 0->1=2, 0->2=3 (via 1), 0->3=6 (via 1->2->3 or 0->1->3? best is 0->1->2->3 with 2+1+3=6)
        assertEquals(0, shortest.get(0));
        assertEquals(2, shortest.get(1));
        assertEquals(3, shortest.get(2));
        assertEquals(6, shortest.get(3));

        Map<Integer, Integer> longest = dsp.longestPaths(0, topo);
        // Expected longest distances: 0->0=0, 0->1=2, 0->2=4 (direct), 0->3=10 (0->1->3 is 2+7=9, 0->2->3 is 4+3=7, 0->1->2->3 is 2+1+3=6) => actually the max is 9 (via 1->3)
        assertEquals(0, longest.get(0));
        assertEquals(2, longest.get(1));
        assertEquals(4, longest.get(2));
        assertEquals(9, longest.get(3)); // best path for longest to 3 is 0->1->3 (2+7)
    }
}
