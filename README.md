# Assignment 4 - Smart City Scheduling

## Overview
Implementation of graph algorithms for task scheduling in smart city scenarios:
- Strongly Connected Components (SCC) with Tarjan's algorithm
- Topological Sorting (Kahn's algorithm)
- Shortest/Longest Paths in DAG
- Critical Path Analysis

## Project Structure
src/  
├── main/java/  
│ ├── graph/  
│ │ ├── scc/SCCFinder.java  
│ │ ├── topo/TopologicalSort.java  
│ │ └── dagsp/DAGShortestPaths.java  
│ ├── util/  
│ │ ├── GraphLoader.java  
│ │ ├── GraphGenerator.java
│ │ └── Metrics.java  
│ └── Main.java  
└── test/java/ (JUnit tests)

## Data Generation
- 9 datasets generated in /data/ folder
- Weight model: Edge weights (1-9)
- Sizes: Small (6-10), Medium (10-20), Large (20-50) nodes

## Empirical Results

| Dataset    | Nodes | Edges | SCCs | SCC Time | SP Time | Critical Path Length |
|------------|-------|-------|------|----------|---------|---------------------|
| small_1    | 6     | 6     | 4    | 1ms      | 0ms     | 14                  |
| small_2    | 8     | 25    | 1    | 0ms      | 0ms     | 0                   |
| small_3    | 7     | 35    | 1    | 0ms      | 0ms     | 0                   |
| medium_1   | 14    | 37    | 4    | 0ms      | 0ms     | 12                  |
| medium_2   | 20    | 205   | 1    | 0ms      | 0ms     | 0                   |
| medium_3   | 20    | 305   | 1    | 0ms      | 0ms     | 0                   |
| large_1    | 47    | 416   | 1    | 0ms      | 0ms     | 0                   |
| large_2    | 28    | 357   | 1    | 0ms      | 0ms     | 0                   |
| large_3    | 40    | 1250  | 1    | 0ms      | 0ms     | 0                   |

### Analysis of Results

**Graph Structure Patterns:**
- **Sparse graphs** (small_1, medium_1): Multiple SCCs, detectable critical paths
- **Dense graphs** (small_2, small_3, medium_2, etc.): Single SCC (strongly connected), critical path length 0

**Performance Observations:**
- All algorithms execute in sub-millisecond time (< 1ms)
- SCC detection handles up to 1250 edges efficiently
- Critical path length varies based on graph connectivity

**Key Insights:**
- Dense graphs tend to form single strongly connected components
- Critical path analysis most meaningful in graphs with multiple SCCs
- Algorithms demonstrate excellent scalability even for large dense graphs

### Performance Summary by Category

| Graph Type | Avg Nodes | Avg Edges | Avg SCCs | Avg Time |
|------------|-----------|-----------|----------|----------|
| Sparse     | 10        | 22        | 4        | 0.5ms    |
| Dense      | 26        | 366       | 1        | 0ms      |

### Conclusions
1. **Algorithm Efficiency**: All graph algorithms execute in sub-millisecond time
2. **Scalability**: Handles graphs up to 40 nodes and 1250 edges efficiently
3. **Graph Density Impact**: Dense graphs form single SCCs, sparse graphs show multiple components
4. **Critical Path Utility**: Most valuable for task scheduling in sparse graphs with dependencies