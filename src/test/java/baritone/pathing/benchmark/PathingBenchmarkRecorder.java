package baritone.pathing.benchmark;

import baritone.scenario.ScenarioPathfinder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Aggregates benchmark results and exports them as CSV and JSON for manual tuning.
 */
public final class PathingBenchmarkRecorder {

    public static final class BenchmarkEntry {
        private final String scenarioId;
        private final int iteration;
        private final int pathLength;
        private final int resourceUsage;
        private final ScenarioPathfinder.ScenarioMetrics metrics;

        public BenchmarkEntry(String scenarioId, int iteration, int pathLength, int resourceUsage, ScenarioPathfinder.ScenarioMetrics metrics) {
            this.scenarioId = scenarioId;
            this.iteration = iteration;
            this.pathLength = pathLength;
            this.resourceUsage = resourceUsage;
            this.metrics = metrics;
        }

        public String scenarioId() {
            return scenarioId;
        }

        public int iteration() {
            return iteration;
        }

        public int pathLength() {
            return pathLength;
        }

        public int resourceUsage() {
            return resourceUsage;
        }

        public ScenarioPathfinder.ScenarioMetrics metrics() {
            return metrics;
        }
    }

    private final List<BenchmarkEntry> entries = new ArrayList<>();

    public void record(BenchmarkEntry entry) {
        entries.add(entry);
    }

    public List<BenchmarkEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public void export(Path outputDirectory) throws IOException {
        Files.createDirectories(outputDirectory);
        writeCsv(outputDirectory.resolve("metrics.csv"));
        writeJson(outputDirectory.resolve("metrics.json"));
    }

    private void writeCsv(Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("scenario,iteration,path_length,resource_usage,compute_nanos,nodes_expanded,hazard_checks,stuck_recoveries,cache_hit\n");
        for (BenchmarkEntry entry : entries) {
            ScenarioPathfinder.ScenarioMetrics metrics = entry.metrics();
            builder.append(entry.scenarioId()).append(',')
                .append(entry.iteration()).append(',')
                .append(entry.pathLength()).append(',')
                .append(entry.resourceUsage()).append(',')
                .append(metrics.computationNanos()).append(',')
                .append(metrics.nodesExpanded()).append(',')
                .append(metrics.hazardEvaluations()).append(',')
                .append(metrics.stuckRecoveries()).append(',')
                .append(metrics.cacheHit()).append('\n');
        }
        Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void writeJson(Path path) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        StringJoiner joiner = new StringJoiner(",");
        for (BenchmarkEntry entry : entries) {
            ScenarioPathfinder.ScenarioMetrics metrics = entry.metrics();
            StringBuilder object = new StringBuilder();
            object.append('{')
                .append("\"scenario\":\"").append(entry.scenarioId()).append("\",")
                .append("\"iteration\":").append(entry.iteration()).append(',')
                .append("\"pathLength\":").append(entry.pathLength()).append(',')
                .append("\"resourceUsage\":").append(entry.resourceUsage()).append(',')
                .append("\"computeNanos\":").append(metrics.computationNanos()).append(',')
                .append("\"nodesExpanded\":").append(metrics.nodesExpanded()).append(',')
                .append("\"hazardEvaluations\":").append(metrics.hazardEvaluations()).append(',')
                .append("\"stuckRecoveries\":").append(metrics.stuckRecoveries()).append(',')
                .append("\"cacheHit\":").append(metrics.cacheHit())
                .append('}');
            joiner.add(object.toString());
        }
        builder.append(joiner.toString());
        builder.append(']');
        Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8));
    }
}
