package baritone.scenario;

import java.util.List;

/**
 * Simulates the survival interrupt logic for the cave scenario.
 */
public final class SurvivalInterruptSimulator {

    private final int hazardExposureThreshold;

    public SurvivalInterruptSimulator(int hazardExposureThreshold) {
        this.hazardExposureThreshold = hazardExposureThreshold;
    }

    public boolean shouldInterrupt(ScenarioMap map, List<GridPoint> path) {
        int consecutiveHazardChecks = 0;
        for (GridPoint point : path) {
            if (isHazardNearby(map, point)) {
                consecutiveHazardChecks++;
                if (consecutiveHazardChecks >= hazardExposureThreshold) {
                    return true;
                }
            } else {
                consecutiveHazardChecks = 0;
            }
        }
        return false;
    }

    private boolean isHazardNearby(ScenarioMap map, GridPoint point) {
        if (map.isHazard(point)) {
            return true;
        }
        for (GridPoint neighbor : map.neighbors(point)) {
            if (map.isHazard(neighbor)) {
                return true;
            }
        }
        return false;
    }
}
