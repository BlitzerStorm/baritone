package baritone.pathing.benchmark;

import baritone.scenario.CachedScenarioPathfinder;
import baritone.scenario.GridPoint;
import baritone.scenario.ScenarioMap;
import baritone.scenario.ScenarioMapFactory;
import baritone.scenario.ScenarioPathCache;
import baritone.scenario.ScenarioPathfinder;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Lightweight benchmark suite that exercises the scenario pathfinder and records metrics.
 */
public final class PathingBenchmarkTest {

    @Test
    public void benchmarkScenariosExportMetrics() throws IOException {
        ScenarioPathfinder pathfinder = new ScenarioPathfinder();
        ScenarioPathCache cache = new ScenarioPathCache();
        CachedScenarioPathfinder cached = new CachedScenarioPathfinder(pathfinder, cache);
        PathingBenchmarkRecorder recorder = new PathingBenchmarkRecorder();

        runScenario(recorder, cached, ScenarioMapFactory.createFlatScenario(), new GridPoint(0, 0), new GridPoint(8, 8), true, 3);
        runScenario(recorder, cached, ScenarioMapFactory.createHillyScenario(), new GridPoint(0, 0), new GridPoint(6, 6), true, 3);
        runScenario(recorder, cached, ScenarioMapFactory.createCaveScenario(), new GridPoint(1, 1), new GridPoint(2, 5), true, 3);

        Path outputDir = Paths.get(System.getProperty("baritone.benchmark.output", "build/benchmarks"));
        recorder.export(outputDir);

        assertEquals("Three scenarios with three iterations each should be recorded", 9, recorder.entries().size());
        assertTrue("CSV metrics should be exported", Files.exists(outputDir.resolve("metrics.csv")));
        assertTrue("JSON metrics should be exported", Files.exists(outputDir.resolve("metrics.json")));
        assertTrue("Benchmarks should prime the cache", cache.size() >= 3);
    }

    private void runScenario(PathingBenchmarkRecorder recorder, CachedScenarioPathfinder cached, ScenarioMap map, GridPoint start, GridPoint goal, boolean avoidHazards, int iterations) {
        for (int iteration = 0; iteration < iterations; iteration++) {
            ScenarioPathfinder.Result result = cached.findPath(map, start, goal, avoidHazards);
            assertFalse("Scenario should produce a path", result.path().isEmpty());
            int resourceUsage = map.estimateResourceUsage(result.path());
            recorder.record(new PathingBenchmarkRecorder.BenchmarkEntry(map.scenarioId(), iteration, result.path().size(), resourceUsage, result.metrics()));
        }
    }
}
