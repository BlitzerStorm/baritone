package baritone.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representation of a lightweight 2D map used exclusively by the scenario driven tests.
 */
public final class ScenarioMap {

    /**
     * Terrain cell describing elevation and environmental attributes for a single tile.
     */
    public static final class Cell {
        private final int elevation;
        private final boolean hazard;
        private final boolean resource;
        private final boolean solid;

        private Cell(int elevation, boolean hazard, boolean resource, boolean solid) {
            this.elevation = elevation;
            this.hazard = hazard;
            this.resource = resource;
            this.solid = solid;
        }

        public static Cell of(int elevation, boolean hazard, boolean resource, boolean solid) {
            return new Cell(elevation, hazard, resource, solid);
        }

        public int elevation() {
            return elevation;
        }

        public boolean hazard() {
            return hazard;
        }

        public boolean resource() {
            return resource;
        }

        public boolean solid() {
            return solid;
        }
    }

    private final String scenarioId;
    private final Cell[][] cells;

    public ScenarioMap(String scenarioId, Cell[][] cells) {
        this.scenarioId = Objects.requireNonNull(scenarioId, "scenarioId");
        this.cells = Objects.requireNonNull(cells, "cells");
    }

    public String scenarioId() {
        return scenarioId;
    }

    public int width() {
        return cells.length;
    }

    public int height() {
        return cells[0].length;
    }

    public Cell cell(int x, int z) {
        if (!isWithinBounds(x, z)) {
            throw new IllegalArgumentException("Coordinates out of bounds: (" + x + "," + z + ")");
        }
        return cells[x][z];
    }

    public boolean isWithinBounds(int x, int z) {
        return x >= 0 && z >= 0 && x < width() && z < height();
    }

    public boolean isPassable(GridPoint point) {
        return isWithinBounds(point.x(), point.z()) && !cell(point.x(), point.z()).solid();
    }

    public boolean isHazard(GridPoint point) {
        if (!isWithinBounds(point.x(), point.z())) {
            return false;
        }
        return cell(point.x(), point.z()).hazard();
    }

    public boolean isResource(GridPoint point) {
        if (!isWithinBounds(point.x(), point.z())) {
            return false;
        }
        return cell(point.x(), point.z()).resource();
    }

    public int elevation(GridPoint point) {
        return cell(point.x(), point.z()).elevation();
    }

    public List<GridPoint> neighbors(GridPoint point) {
        List<GridPoint> neighbors = new ArrayList<>(4);
        int x = point.x();
        int z = point.z();
        addIfPassable(neighbors, x + 1, z);
        addIfPassable(neighbors, x - 1, z);
        addIfPassable(neighbors, x, z + 1);
        addIfPassable(neighbors, x, z - 1);
        return neighbors;
    }

    public GridPoint clampToPassable(int x, int z) {
        GridPoint direct = new GridPoint(x, z);
        if (isPassable(direct) && !isHazard(direct)) {
            return direct;
        }
        for (GridPoint neighbor : neighbors(direct)) {
            if (!isHazard(neighbor)) {
                return neighbor;
            }
        }
        // If nothing else works, just stay in bounds.
        int clampedX = Math.max(0, Math.min(width() - 1, x));
        int clampedZ = Math.max(0, Math.min(height() - 1, z));
        return new GridPoint(clampedX, clampedZ);
    }

    public int traversalCost(GridPoint from, GridPoint to) {
        int elevationDelta = Math.abs(elevation(to) - elevation(from));
        return 1 + elevationDelta;
    }

    public int estimateResourceUsage(List<GridPoint> path) {
        if (path.isEmpty()) {
            return 0;
        }
        int cost = 0;
        GridPoint previous = path.get(0);
        for (int i = 1; i < path.size(); i++) {
            GridPoint step = path.get(i);
            cost += traversalCost(previous, step);
            previous = step;
        }
        return cost;
    }

    public List<GridPoint> locateResources() {
        List<GridPoint> found = new ArrayList<>();
        for (int x = 0; x < width(); x++) {
            for (int z = 0; z < height(); z++) {
                if (cells[x][z].resource()) {
                    found.add(new GridPoint(x, z));
                }
            }
        }
        return Collections.unmodifiableList(found);
    }

    private void addIfPassable(List<GridPoint> neighbors, int x, int z) {
        if (isWithinBounds(x, z) && !cells[x][z].solid()) {
            neighbors.add(new GridPoint(x, z));
        }
    }
}
