package graph.scc;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SCCFinderTest {

    @Test
    public void simpleSCCs() {
        Map<Integer, List<Integer>> g = new HashMap<>();
        g.put(0, List.of(1));
        g.put(1, List.of(2));
        g.put(2, List.of(0));
        g.put(3, List.of(4));
        g.put(4, List.of(3));
        g.put(5, List.of());

        SCCFinder finder = new SCCFinder(g);
        List<List<Integer>> sccs = finder.findSCCs();

        assertEquals(3, sccs.size(), "Should find 3 SCCs");

        Set<Set<Integer>> setOfSCCs = new HashSet<>();
        for (List<Integer> comp : sccs) setOfSCCs.add(new HashSet<>(comp));

        assertTrue(setOfSCCs.contains(new HashSet<>(List.of(0,1,2))));
        assertTrue(setOfSCCs.contains(new HashSet<>(List.of(3,4))));
        assertTrue(setOfSCCs.contains(new HashSet<>(List.of(5))));
    }

    @Test
    public void emptyGraph() {
        Map<Integer, List<Integer>> g = new HashMap<>();
        SCCFinder finder = new SCCFinder(g);
        List<List<Integer>> sccs = finder.findSCCs();
        assertEquals(0, sccs.size());
    }

    @Test
    public void singleNode() {
        Map<Integer, List<Integer>> g = new HashMap<>();
        g.put(0, List.of());
        SCCFinder finder = new SCCFinder(g);
        List<List<Integer>> sccs = finder.findSCCs();
        assertEquals(1, sccs.size());
        assertEquals(1, sccs.get(0).size());
        assertEquals(0, sccs.get(0).get(0));
    }

    @Test
    public void disconnectedNodes() {
        Map<Integer, List<Integer>> g = new HashMap<>();
        g.put(0, List.of());
        g.put(1, List.of());
        g.put(2, List.of());
        SCCFinder finder = new SCCFinder(g);
        List<List<Integer>> sccs = finder.findSCCs();
        assertEquals(3, sccs.size());
    }

    @Test
    public void completeGraph() {
        Map<Integer, List<Integer>> g = new HashMap<>();
        g.put(0, List.of(1, 2));
        g.put(1, List.of(0, 2));
        g.put(2, List.of(0, 1));
        SCCFinder finder = new SCCFinder(g);
        List<List<Integer>> sccs = finder.findSCCs();
        assertEquals(1, sccs.size());
        assertEquals(3, sccs.get(0).size());
    }
}
