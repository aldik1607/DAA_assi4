package graph.dagsp;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class DAGShortestPathsTest {

    @Test
    public void shortestAndLongestSimple() {
        Map<Integer, List<int[]>> dag = new HashMap<>();
        dag.put(0, new ArrayList<>(List.of(new int[]{1,2}, new int[]{2,4})));
        dag.put(1, new ArrayList<>(List.of(new int[]{2,1}, new int[]{3,7})));
        dag.put(2, new ArrayList<>(List.of(new int[]{3,3})));
        dag.put(3, new ArrayList<>());

        List<Integer> topo = List.of(0,1,2,3);

        DAGShortestPaths dsp = new DAGShortestPaths(dag);

        Map<Integer, Integer> shortest = dsp.shortestPaths(0, topo);
        assertEquals(0, shortest.get(0));
        assertEquals(2, shortest.get(1));
        assertEquals(3, shortest.get(2));
        assertEquals(6, shortest.get(3));

        Map<Integer, Integer> longest = dsp.longestPaths(0, topo);
        assertEquals(0, longest.get(0));
        assertEquals(2, longest.get(1));
        assertEquals(4, longest.get(2));
        assertEquals(9, longest.get(3));
    }

    @Test
    public void criticalPathTest() {
        Map<Integer, List<int[]>> dag = new HashMap<>();
        dag.put(0, new ArrayList<>(List.of(new int[]{1, 2}, new int[]{2, 4})));
        dag.put(1, new ArrayList<>(List.of(new int[]{2, 1}, new int[]{3, 7})));
        dag.put(2, new ArrayList<>(List.of(new int[]{3, 3})));
        dag.put(3, new ArrayList<>());

        List<Integer> topo = List.of(0, 1, 2, 3);

        DAGShortestPaths dsp = new DAGShortestPaths(dag);
        CriticalPathResult criticalPath = dsp.findCriticalPath(topo);

        assertEquals(9, criticalPath.length());
        assertFalse(criticalPath.path().isEmpty());

        List<Integer> path = criticalPath.path();
        assertTrue(path.contains(0) && path.contains(3));
    }

    @Test
    public void emptyGraph() {
        Map<Integer, List<int[]>> dag = new HashMap<>();
        List<Integer> topo = List.of();
        DAGShortestPaths dsp = new DAGShortestPaths(dag);

        Map<Integer, Integer> shortest = dsp.shortestPaths(0, topo);
        Map<Integer, Integer> longest = dsp.longestPaths(0, topo);

        assertTrue(shortest.isEmpty() || shortest.get(0) == 0);
        assertTrue(longest.isEmpty() || longest.get(0) == 0);
    }

    @Test
    public void singleNodeGraph() {
        Map<Integer, List<int[]>> dag = new HashMap<>();
        dag.put(0, List.of());
        List<Integer> topo = List.of(0);
        DAGShortestPaths dsp = new DAGShortestPaths(dag);

        Map<Integer, Integer> shortest = dsp.shortestPaths(0, topo);
        Map<Integer, Integer> longest = dsp.longestPaths(0, topo);

        assertEquals(0, shortest.get(0));
        assertEquals(0, longest.get(0));
    }

    @Test
    public void unreachableNodes() {
        Map<Integer, List<int[]>> dag = new HashMap<>();
        dag.put(0, List.of(new int[]{1, 5}));
        dag.put(1, List.of());
        dag.put(2, List.of()); // unreachable from 0
        List<Integer> topo = List.of(0, 1, 2);
        DAGShortestPaths dsp = new DAGShortestPaths(dag);

        Map<Integer, Integer> shortest = dsp.shortestPaths(0, topo);
        Map<Integer, Integer> longest = dsp.longestPaths(0, topo);

        assertEquals(Integer.MAX_VALUE, shortest.get(2));
        assertEquals(Integer.MIN_VALUE, longest.get(2));
    }

    @Test
    public void multiplePaths() {
        Map<Integer, List<int[]>> dag = new HashMap<>();
        dag.put(0, List.of(new int[]{1, 1}, new int[]{2, 5}));
        dag.put(1, List.of(new int[]{2, 1}));
        dag.put(2, List.of());
        List<Integer> topo = List.of(0, 1, 2);
        DAGShortestPaths dsp = new DAGShortestPaths(dag);

        Map<Integer, Integer> shortest = dsp.shortestPaths(0, topo);
        Map<Integer, Integer> longest = dsp.longestPaths(0, topo);

        assertEquals(2, shortest.get(2));
        assertEquals(5, longest.get(2));
    }

    @Test
    public void criticalPathSingleNode() {
        Map<Integer, List<int[]>> dag = new HashMap<>();
        dag.put(0, List.of());
        List<Integer> topo = List.of(0);
        DAGShortestPaths dsp = new DAGShortestPaths(dag);

        CriticalPathResult criticalPath = dsp.findCriticalPath(topo);
        assertEquals(List.of(0), criticalPath.path());
        assertEquals(0, criticalPath.length());
    }


}