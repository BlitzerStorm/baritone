package baritone.utils.pathing;

import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.goals.Goal;
import baritone.api.utils.BetterBlockPos;
import baritone.pathing.movement.CalculationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Simple LRU cache for storing short-lived path segments that have recently succeeded.
 */
public final class PathSegmentCache {

    private final Map<PathCacheKey, CachedPath> cache = new HashMap<>();
    private final Deque<PathCacheKey> order = new ArrayDeque<>();

    public synchronized Optional<IPath> lookup(BetterBlockPos start, Goal goal, CalculationContext context, double startToleranceSq, double goalEpsilon) {
        if (cache.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation dimension = context.world.dimension().location();
        PathCacheKey candidate = null;
        double bestHeuristic = Double.MAX_VALUE;
        for (PathCacheKey key : order) {
            CachedPath cached = cache.get(key);
            if (cached == null || !cached.dimension.equals(dimension) || !cached.goalClass.equals(goal.getClass())) {
                continue;
            }
            if (!cached.matches(start, goal, startToleranceSq, goalEpsilon)) {
                continue;
            }
            double heur = goal.heuristic(cached.path.getDest().x, cached.path.getDest().y, cached.path.getDest().z);
            if (heur < bestHeuristic) {
                bestHeuristic = heur;
                candidate = key;
            }
        }
        if (candidate == null) {
            return Optional.empty();
        }
        promote(candidate);
        return Optional.of(cache.get(candidate).path);
    }

    public synchronized void store(BetterBlockPos start, Goal goal, CalculationContext context, IPath path, int maxEntries) {
        if (path == null || maxEntries <= 0) {
            return;
        }
        ResourceLocation dimension = context.world.dimension().location();
        PathCacheKey key = new PathCacheKey(dimension, goal.getClass(), start);
        CachedPath cached = new CachedPath(dimension, goal.getClass(), start, path);
        cache.put(key, cached);
        promote(key);
        trim(maxEntries);
    }

    public synchronized void invalidateAround(BlockPos pos, double radiusSq) {
        if (cache.isEmpty()) {
            return;
        }
        cache.entrySet().removeIf(entry -> entry.getValue().isNear(pos, radiusSq));
        order.removeIf(key -> !cache.containsKey(key));
    }

    public synchronized void clear() {
        cache.clear();
        order.clear();
    }

    private void trim(int maxEntries) {
        while (order.size() > maxEntries) {
            PathCacheKey key = order.removeLast();
            cache.remove(key);
        }
    }

    private void promote(PathCacheKey key) {
        order.remove(key);
        order.addFirst(key);
    }

    private record PathCacheKey(ResourceLocation dimension, Class<? extends Goal> goalClass, BetterBlockPos start) {
    }

    private static final class CachedPath {
        private final ResourceLocation dimension;
        private final Class<? extends Goal> goalClass;
        private final BetterBlockPos start;
        private final IPath path;

        private CachedPath(ResourceLocation dimension, Class<? extends Goal> goalClass, BetterBlockPos start, IPath path) {
            this.dimension = dimension;
            this.goalClass = goalClass;
            this.start = start;
            this.path = path;
        }

        private boolean matches(BetterBlockPos candidateStart, Goal goal, double startToleranceSq, double goalEpsilon) {
            if (!Objects.equals(goalClass, goal.getClass())) {
                return false;
            }
            if (candidateStart.distanceSq(start) > startToleranceSq) {
                return false;
            }
            BetterBlockPos destination = path.getDest();
            if (destination == null) {
                return false;
            }
            double heuristic = goal.heuristic(destination.x, destination.y, destination.z);
            return heuristic <= goalEpsilon;
        }

        private boolean isNear(BlockPos pos, double radiusSq) {
            BetterBlockPos destination = path.getDest();
            if (destination == null) {
                return false;
            }
            return destination.distanceSq(pos) <= radiusSq || start.distanceSq(pos) <= radiusSq;
        }
    }
}
