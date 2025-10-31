package graph.scc;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SCCFinderTest {

    @Test
    public void simpleSCCs() {
        // Graph:
        // 0 -> 1
        // 1 -> 2
        // 2 -> 0   (0,1,2) - cycle
        // 3 -> 4
        // 4 -> 3   (3,4) - cycle
        // 5 isolated
        Map<Integer, List<Integer>> g = new HashMap<>();
        g.put(0, List.of(1));
        g.put(1, List.of(2));
        g.put(2, List.of(0));
        g.put(3, List.of(4));
        g.put(4, List.of(3));
        g.put(5, List.of());

        SCCFinder finder = new SCCFinder(g);
        List<List<Integer>> sccs = finder.findSCCs();

        // We expect 3 components: {0,1,2}, {3,4}, {5} (order may vary)
        assertEquals(3, sccs.size(), "Should find 3 SCCs");

        // Convert to set-of-sets for easier assertions
        Set<Set<Integer>> setOfSCCs = new HashSet<>();
        for (List<Integer> comp : sccs) setOfSCCs.add(new HashSet<>(comp));

        assertTrue(setOfSCCs.contains(new HashSet<>(List.of(0,1,2))));
        assertTrue(setOfSCCs.contains(new HashSet<>(List.of(3,4))));
        assertTrue(setOfSCCs.contains(new HashSet<>(List.of(5))));
    }
}
