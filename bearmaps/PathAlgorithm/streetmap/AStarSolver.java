package bearmaps.PathAlgorithm.streetmap;

import bearmaps.PathAlgorithm.AStarGraph;
import bearmaps.PathAlgorithm.ShortestPathsSolver;
import bearmaps.PathAlgorithm.SolverOutcome;
import bearmaps.PathAlgorithm.WeightedEdge;
import bearmaps.Coordinate.DoubleMapPQ;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {

    private SolverOutcome outcome;
    private double spentTime;
    private LinkedList<Vertex> result;
    private double totalWeight;
    private int dequeueCount;


    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        Stopwatch sw = new Stopwatch();
        result = new LinkedList<>();

        HashMap<Vertex, Vertex> edgeTo = new HashMap<>();
        HashMap<Vertex, Double> disTo = new HashMap<>();
        DoubleMapPQ<Vertex> PQ = new DoubleMapPQ<>();

        totalWeight = 0;
        dequeueCount = 0;

        disTo.put(start, 0.0);
        PQ.add(start, disTo.get(start) + input.estimatedDistanceToGoal(start, end));

        while (PQ.size() > 0 && !PQ.getSmallest().equals(end) && sw.elapsedTime() < timeout) {
            Vertex tmp = PQ.removeSmallest();
            dequeueCount++;
            for (WeightedEdge<Vertex> e : input.neighbors(tmp)) {
                Vertex q = e.to();
                double w = e.weight();

                if (!disTo.containsKey(q) || disTo.get(tmp) + w < disTo.get(q)) {
                    disTo.put(q, disTo.get(tmp) + w);
                    edgeTo.put(q, tmp);
                    if (PQ.contains(q)) {
                        PQ.changePriority(q, disTo.get(q) + input.estimatedDistanceToGoal(q, end));
                    } else {
                        PQ.add(q, disTo.get(q) + input.estimatedDistanceToGoal(q, end));
                    }
                }
            }
        }
        spentTime = sw.elapsedTime();
        if (PQ.size() == 0) {
            outcome = SolverOutcome.UNSOLVABLE;
        } else if (PQ.getSmallest().equals(end)) {
            outcome = SolverOutcome.SOLVED;
            Vertex target = end;
            while (target != start) {
                result.addFirst(target);
                target = edgeTo.get(target);
            }
            result.add(0, start);
            totalWeight = disTo.get(end);
        } else {
            outcome = SolverOutcome.TIMEOUT;
        }

    }

    @Override
    public SolverOutcome outcome() {
        return outcome;
    }

    @Override
    public List<Vertex> solution() {
        if (outcome == SolverOutcome.SOLVED) return result;
        return null;
    }

    @Override
    public double solutionWeight() {
        if (outcome == SolverOutcome.SOLVED) return totalWeight;
        return 0;
    }

    @Override
    public int numStatesExplored() {
        if (outcome == SolverOutcome.SOLVED) return dequeueCount;
        return 0;
    }

    @Override
    public double explorationTime() {
        return spentTime;
    }
}
