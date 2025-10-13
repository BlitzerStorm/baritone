package baritone.scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates circular follow routes for the hilly terrain tests.
 */
public final class FollowCircleSimulator {

    private FollowCircleSimulator() {
    }

    public static List<GridPoint> computeOrbit(ScenarioMap map, GridPoint center, int radius, int steps) {
        List<GridPoint> orbit = new ArrayList<>(steps);
        for (int i = 0; i < steps; i++) {
            double angle = (2 * Math.PI * i) / steps;
            int x = center.x() + (int) Math.round(radius * Math.cos(angle));
            int z = center.z() + (int) Math.round(radius * Math.sin(angle));
            orbit.add(map.clampToPassable(x, z));
        }
        return orbit;
    }
}
