package graph.topo;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TopologicalSortTest {

    @Test
    public void kahnSimple() {
        // DAG:
        // 0 -> 1, 0->2, 1->3, 2->3
        Map<Integer, List<Integer>> dag = new HashMap<>();
        dag.put(0, List.of(1,2));
        dag.put(1, List.of(3));
        dag.put(2, List.of(3));
        dag.put(3, List.of());

        TopologicalSort topo = new TopologicalSort();
        List<Integer> order = topo.sort(dag);

        // Validate: 0 must come before 1 and 2; 1 and 2 before 3
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }
}
