package graph.dagsp;

import java.util.List;

public record CriticalPathResult(List<Integer> path, int length) {
    @Override
    public String toString() {
        return "CriticalPath{path=" + path + ", length=" + length + "}";
    }
}