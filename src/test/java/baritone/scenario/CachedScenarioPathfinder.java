package baritone.scenario;

import java.util.Objects;

/**
 * Decorator that adds caching behaviour on top of {@link ScenarioPathfinder}.
 */
public final class CachedScenarioPathfinder {

    private final ScenarioPathfinder delegate;
    private final ScenarioPathCache cache;

    public CachedScenarioPathfinder(ScenarioPathfinder delegate, ScenarioPathCache cache) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    public ScenarioPathfinder.Result findPath(ScenarioMap map, GridPoint start, GridPoint goal, boolean avoidHazards) {
        ScenarioPathfinder.Result cachedResult = cache.get(map.scenarioId(), start, goal, avoidHazards);
        if (cachedResult != null) {
            return cachedResult.copyWithCacheHit();
        }
        ScenarioPathfinder.Result result = delegate.findPath(map, start, goal, avoidHazards);
        cache.put(map.scenarioId(), start, goal, avoidHazards, result);
        return result;
    }
}
