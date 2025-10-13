package baritone;

import baritone.scenario.CachedScenarioPathfinder;
import baritone.scenario.FollowCircleSimulator;
import baritone.scenario.GridPoint;
import baritone.scenario.ScenarioMap;
import baritone.scenario.ScenarioMapFactory;
import baritone.scenario.ScenarioPathCache;
import baritone.scenario.ScenarioPathfinder;
import baritone.scenario.SurvivalInterruptSimulator;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Scenario driven tests that emulate high level behaviour without touching the main game runtime.
 */
public final class ScenarioDrivenBehaviorTest {

    @Test
    public void hazardAwareMiningAvoidsHazardsOnFlatMap() {
        ScenarioMap map = ScenarioMapFactory.createFlatScenario();
        ScenarioPathfinder pathfinder = new ScenarioPathfinder();
        GridPoint start = new GridPoint(0, 0);
        GridPoint goal = new GridPoint(8, 8);

        ScenarioPathfinder.Result result = pathfinder.findPath(map, start, goal, true);

        assertFalse("A path should be produced across the flat terrain", result.path().isEmpty());
        assertTrue("No hazard blocks should appear in the path", result.path().stream().noneMatch(map::isHazard));
        assertTrue("Hazard avoidance should require non-zero evaluations", result.metrics().hazardEvaluations() > 0);
        assertTrue("Avoidance should have triggered at least one recovery", result.metrics().stuckRecoveries() > 0);
    }

    @Test
    public void followCirclingMaintainsOrbitOnHillyMap() {
        ScenarioMap map = ScenarioMapFactory.createHillyScenario();
        GridPoint target = new GridPoint(3, 3);
        List<GridPoint> orbit = FollowCircleSimulator.computeOrbit(map, target, 2, 12);

        assertEquals("Expected twelve steps around the target", 12, orbit.size());
        Set<GridPoint> visited = new HashSet<>(orbit);
        assertTrue("Orbit should include multiple unique points", visited.size() > 6);
        for (GridPoint point : orbit) {
            double distance = point.distanceTo(target);
            assertTrue("Follow circle should stay close to the requested radius", distance >= 1.5 && distance <= 2.5);
            assertFalse("Follow behaviour must avoid hazards on the ridge", map.isHazard(point));
        }
    }

    @Test
    public void survivalInterruptsTriggerInCaveWhenHazardsIgnored() {
        ScenarioMap map = ScenarioMapFactory.createCaveScenario();
        ScenarioPathfinder pathfinder = new ScenarioPathfinder();
        GridPoint start = new GridPoint(1, 1);
        GridPoint goal = new GridPoint(2, 5);

        ScenarioPathfinder.Result risky = pathfinder.findPath(map, start, goal, false);
        assertTrue("The risky run should take the most direct route", risky.path().size() < 12);
        SurvivalInterruptSimulator interrupts = new SurvivalInterruptSimulator(2);
        assertTrue("Ignoring hazards in a cave should trigger an interrupt", interrupts.shouldInterrupt(map, risky.path()));

        ScenarioPathfinder.Result safe = pathfinder.findPath(map, start, goal, true);
        assertFalse("The safe path should route around lava pockets", interrupts.shouldInterrupt(map, safe.path()));
        assertTrue("The safe path should still reach the target", map.isResource(safe.path().get(safe.path().size() - 1)));
    }

    @Test
    public void cachedPathReuseIsReportedAcrossRuns() {
        ScenarioMap map = ScenarioMapFactory.createHillyScenario();
        ScenarioPathfinder pathfinder = new ScenarioPathfinder();
        ScenarioPathCache cache = new ScenarioPathCache();
        CachedScenarioPathfinder cached = new CachedScenarioPathfinder(pathfinder, cache);
        GridPoint start = new GridPoint(0, 0);
        GridPoint goal = new GridPoint(6, 6);

        ScenarioPathfinder.Result first = cached.findPath(map, start, goal, true);
        ScenarioPathfinder.Result second = cached.findPath(map, start, goal, true);

        assertFalse(first.metrics().cacheHit());
        assertTrue(second.metrics().cacheHit());
        assertEquals("Cache should contain a single entry", 1, cache.size());
        assertEquals("Second call should reuse the same logical path", first.path(), second.path());
        assertEquals("Resource usage estimation should be deterministic", map.estimateResourceUsage(first.path()), map.estimateResourceUsage(second.path()));
    }
}
