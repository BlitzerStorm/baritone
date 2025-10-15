package baritone.scenario;

/**
 * Factories that assemble representative maps for flat, hilly, and cave mining scenarios.
 */
public final class ScenarioMapFactory {

    private ScenarioMapFactory() {
    }

    public static ScenarioMap createFlatScenario() {
        ScenarioMap.Cell[][] cells = new ScenarioMap.Cell[9][9];
        for (int x = 0; x < cells.length; x++) {
            for (int z = 0; z < cells[x].length; z++) {
                cells[x][z] = ScenarioMap.Cell.of(1, false, false, false);
            }
        }
        // carve a hazard band through the center to stress hazard avoidance
        for (int x = 2; x <= 6; x++) {
            cells[x][4] = ScenarioMap.Cell.of(1, true, false, false);
        }
        // designate the resource on the far side of the hazard band
        cells[8][8] = ScenarioMap.Cell.of(1, false, true, false);
        return new ScenarioMap("flat", cells);
    }

    public static ScenarioMap createHillyScenario() {
        ScenarioMap.Cell[][] cells = new ScenarioMap.Cell[7][7];
        for (int x = 0; x < cells.length; x++) {
            for (int z = 0; z < cells[x].length; z++) {
                int elevation = (x + z) % 4;
                cells[x][z] = ScenarioMap.Cell.of(1 + elevation, false, false, false);
            }
        }
        // Provide a few hazards on peaks to encourage circling logic.
        cells[3][3] = ScenarioMap.Cell.of(5, true, false, false);
        cells[5][1] = ScenarioMap.Cell.of(4, true, false, false);
        cells[1][5] = ScenarioMap.Cell.of(4, true, false, false);
        cells[6][6] = ScenarioMap.Cell.of(6, false, true, false);
        return new ScenarioMap("hilly", cells);
    }

    public static ScenarioMap createCaveScenario() {
        ScenarioMap.Cell[][] cells = new ScenarioMap.Cell[8][8];
        for (int x = 0; x < cells.length; x++) {
            for (int z = 0; z < cells[x].length; z++) {
                boolean solid = x == 0 || z == 0 || x == cells.length - 1 || z == cells[x].length - 1;
                cells[x][z] = ScenarioMap.Cell.of(0, false, false, solid);
            }
        }
        // carve a winding path from entrance to resource
        for (int x = 1; x < 7; x++) {
            cells[x][1] = ScenarioMap.Cell.of(0, false, false, false);
        }
        for (int z = 1; z < 6; z++) {
            cells[6][z] = ScenarioMap.Cell.of(0, false, false, false);
        }
        for (int x = 6; x >= 2; x--) {
            cells[x][5] = ScenarioMap.Cell.of(0, false, false, false);
        }
        cells[2][5] = ScenarioMap.Cell.of(0, false, true, false);
        // hazards (like lava pockets) placed just off the main route
        cells[4][2] = ScenarioMap.Cell.of(0, true, false, false);
        cells[5][4] = ScenarioMap.Cell.of(0, true, false, false);
        return new ScenarioMap("cave", cells);
    }
}
