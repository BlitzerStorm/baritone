package baritone.scenario;

import java.util.Objects;

/**
 * Immutable point on a 2D grid used by the scenario-driven tests.
 */
public final class GridPoint {

    private final int x;
    private final int z;

    public GridPoint(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    public double distanceTo(GridPoint other) {
        Objects.requireNonNull(other, "other");
        int dx = other.x - x;
        int dz = other.z - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public int manhattanDistance(GridPoint other) {
        Objects.requireNonNull(other, "other");
        return Math.abs(other.x - x) + Math.abs(other.z - z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GridPoint)) {
            return false;
        }
        GridPoint other = (GridPoint) obj;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    public String toString() {
        return "GridPoint{" + "x=" + x + ", z=" + z + '}';
    }
}
