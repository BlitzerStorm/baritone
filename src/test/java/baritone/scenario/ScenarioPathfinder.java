package baritone.scenario;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * Simple pathfinder tailored to the scenario driven tests.
 */
public final class ScenarioPathfinder {

    public static final class ScenarioMetrics {
        private final long computationNanos;
        private final int nodesExpanded;
        private final int hazardEvaluations;
        private final int stuckRecoveries;
        private final boolean cacheHit;

        public ScenarioMetrics(long computationNanos, int nodesExpanded, int hazardEvaluations, int stuckRecoveries, boolean cacheHit) {
            this.computationNanos = computationNanos;
            this.nodesExpanded = nodesExpanded;
            this.hazardEvaluations = hazardEvaluations;
            this.stuckRecoveries = stuckRecoveries;
            this.cacheHit = cacheHit;
        }

        public long computationNanos() {
            return computationNanos;
        }

        public int nodesExpanded() {
            return nodesExpanded;
        }

        public int hazardEvaluations() {
            return hazardEvaluations;
        }

        public int stuckRecoveries() {
            return stuckRecoveries;
        }

        public boolean cacheHit() {
            return cacheHit;
        }

        public ScenarioMetrics withCacheHit() {
            return new ScenarioMetrics(computationNanos, nodesExpanded, hazardEvaluations, stuckRecoveries, true);
        }
    }

    public static final class Result {
        private final List<GridPoint> path;
        private final ScenarioMetrics metrics;

        Result(List<GridPoint> path, ScenarioMetrics metrics) {
            this.path = Collections.unmodifiableList(path);
            this.metrics = metrics;
        }

        public List<GridPoint> path() {
            return path;
        }

        public ScenarioMetrics metrics() {
            return metrics;
        }

        public Result copyWithCacheHit() {
            return new Result(new ArrayList<>(path), metrics.withCacheHit());
        }
    }

    private static final class Node implements Comparable<Node> {
        private final GridPoint point;
        private final int cost;
        private final int priority;
        private final Node previous;

        private Node(GridPoint point, int cost, int priority, Node previous) {
            this.point = point;
            this.cost = cost;
            this.priority = priority;
            this.previous = previous;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(priority, other.priority);
        }
    }

    public Result findPath(ScenarioMap map, GridPoint start, GridPoint goal, boolean avoidHazards) {
        Objects.requireNonNull(map, "map");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(goal, "goal");
        long startTime = System.nanoTime();
        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<GridPoint, Integer> bestCost = new HashMap<>();
        Map<GridPoint, Node> reached = new HashMap<>();
        int hazardEvaluations = 0;
        int stuckRecoveries = 0;

        open.add(new Node(start, 0, start.manhattanDistance(goal), null));

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.point.equals(goal)) {
                long computationNanos = System.nanoTime() - startTime;
                List<GridPoint> path = reconstruct(current);
                ScenarioMetrics metrics = new ScenarioMetrics(computationNanos, reached.size() + 1, hazardEvaluations, stuckRecoveries, false);
                return new Result(path, metrics);
            }

            Integer previousCost = bestCost.get(current.point);
            if (previousCost != null && previousCost <= current.cost) {
                continue;
            }
            bestCost.put(current.point, current.cost);
            reached.put(current.point, current);

            for (GridPoint neighbor : map.neighbors(current.point)) {
                boolean hazard = map.isHazard(neighbor);
                if (hazard) {
                    hazardEvaluations++;
                    if (avoidHazards) {
                        stuckRecoveries++;
                        continue;
                    }
                }
                int newCost = current.cost + map.traversalCost(current.point, neighbor) + (hazard ? 5 : 0);
                Integer knownCost = bestCost.get(neighbor);
                if (knownCost != null && knownCost <= newCost) {
                    continue;
                }
                int heuristic = neighbor.manhattanDistance(goal);
                open.add(new Node(neighbor, newCost, newCost + heuristic, current));
            }
        }

        long computationNanos = System.nanoTime() - startTime;
        ScenarioMetrics metrics = new ScenarioMetrics(computationNanos, reached.size(), hazardEvaluations, stuckRecoveries, false);
        return new Result(Collections.emptyList(), metrics);
    }

    private List<GridPoint> reconstruct(Node node) {
        Deque<GridPoint> reversed = new ArrayDeque<>();
        Node cursor = node;
        while (cursor != null) {
            reversed.addFirst(cursor.point);
            cursor = cursor.previous;
        }
        return new ArrayList<>(reversed);
    }
}
