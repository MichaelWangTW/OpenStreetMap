package bearmaps.PathAlgorithm;

import java.util.List;

/**
 * Represents a graph of vertices.
 */
public interface AStarGraph<Vertex> {
    List<WeightedEdge<Vertex>> neighbors(Vertex v);
    double estimatedDistanceToGoal(Vertex s, Vertex goal);
}
