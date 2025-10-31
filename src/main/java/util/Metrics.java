package util;

public class Metrics {
    private long startTime;
    private long endTime;
    private int dfsCount;
    private int relaxations;
    private int topoPushes;

    public void start() {
        startTime = System.nanoTime();
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public void addDFSVisit() {
        dfsCount++;
    }

    public void addRelaxation() {
        relaxations++;
    }

    public void addTopoPush() {
        topoPushes++;
    }

    public long getElapsedTimeMs() {
        return (endTime - startTime) / 1_000_000;
    }

    public int getDfsCount() {
        return dfsCount;
    }

    public int getRelaxations() {
        return relaxations;
    }

    public int getTopoPushes() {
        return topoPushes;
    }

    public void print() {
        System.out.println("DFS visits: " + dfsCount);
        System.out.println("Relaxations: " + relaxations);
        System.out.println("Topological pushes: " + topoPushes);
        System.out.println("Time: " + getElapsedTimeMs() + " ms");
    }
}
