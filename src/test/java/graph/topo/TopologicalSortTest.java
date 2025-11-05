package graph.topo;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TopologicalSortTest {

    @Test
    public void kahnSimple() {

        Map<Integer, List<Integer>> dag = new HashMap<>();
        dag.put(0, List.of(1,2));
        dag.put(1, List.of(3));
        dag.put(2, List.of(3));
        dag.put(3, List.of());

        TopologicalSort topo = new TopologicalSort();
        List<Integer> order = topo.sort(dag);

        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    public void emptyDAG() {
        Map<Integer, List<Integer>> dag = new HashMap<>();
        TopologicalSort topo = new TopologicalSort();
        List<Integer> order = topo.sort(dag);
        assertEquals(0, order.size());
    }

    @Test
    public void singleNodeDAG() {
        Map<Integer, List<Integer>> dag = new HashMap<>();
        dag.put(0, List.of());
        TopologicalSort topo = new TopologicalSort();
        List<Integer> order = topo.sort(dag);
        assertEquals(1, order.size());
        assertEquals(0, order.get(0));
    }

    @Test
    public void linearDAG() {
        Map<Integer, List<Integer>> dag = new HashMap<>();
        dag.put(0, List.of(1));
        dag.put(1, List.of(2));
        dag.put(2, List.of(3));
        dag.put(3, List.of());
        TopologicalSort topo = new TopologicalSort();
        List<Integer> order = topo.sort(dag);
        assertEquals(4, order.size());
        assertEquals(0, order.get(0));
        assertEquals(3, order.get(3));
    }

    @Test
    public void starDAG() {
        Map<Integer, List<Integer>> dag = new HashMap<>();
        dag.put(0, List.of(1, 2, 3));
        dag.put(1, List.of());
        dag.put(2, List.of());
        dag.put(3, List.of());
        TopologicalSort topo = new TopologicalSort();
        List<Integer> order = topo.sort(dag);
        assertEquals(4, order.size());
        assertEquals(0, order.get(0)); // root first
    }
}
