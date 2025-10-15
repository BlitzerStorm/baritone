package baritone.scenario;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Cache for storing scenario pathfinder results keyed by scenario and configuration.
 */
public final class ScenarioPathCache {

    private final Map<RequestKey, ScenarioPathfinder.Result> cached = new HashMap<>();

    public ScenarioPathfinder.Result get(String scenarioId, GridPoint start, GridPoint goal, boolean avoidHazards) {
        return cached.get(new RequestKey(scenarioId, start, goal, avoidHazards));
    }

    public void put(String scenarioId, GridPoint start, GridPoint goal, boolean avoidHazards, ScenarioPathfinder.Result result) {
        cached.put(new RequestKey(scenarioId, start, goal, avoidHazards), result);
    }

    public int size() {
        return cached.size();
    }

    private static final class RequestKey {
        private final String scenarioId;
        private final GridPoint start;
        private final GridPoint goal;
        private final boolean avoidHazards;

        private RequestKey(String scenarioId, GridPoint start, GridPoint goal, boolean avoidHazards) {
            this.scenarioId = scenarioId;
            this.start = start;
            this.goal = goal;
            this.avoidHazards = avoidHazards;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RequestKey)) {
                return false;
            }
            RequestKey other = (RequestKey) obj;
            return avoidHazards == other.avoidHazards
                && Objects.equals(scenarioId, other.scenarioId)
                && Objects.equals(start, other.start)
                && Objects.equals(goal, other.goal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scenarioId, start, goal, avoidHazards);
        }
    }
}
