package bot;

import datastorage.Terrain;
import utility.math.Vector2;

public class PathfindingMapCreator {
    public PathfindingMapCreator(Terrain terrain) {
        this.terrain = terrain;
        checkForNullTerrain();
        setMapCorners();
    }

    private void setMapCorners() {
        int topLeftX = ((int) (terrain.topLeftCorner.x * SQUARES_PER_GAME_UNIT) / SQUARES_PER_GAME_UNIT);
        int topLeftY = ((int) (terrain.topLeftCorner.y * SQUARES_PER_GAME_UNIT) / SQUARES_PER_GAME_UNIT);
        topLeftPos = new Vector2(topLeftX, topLeftY);
        int bottomRightX = ((int) (terrain.bottomRightCorner.x * SQUARES_PER_GAME_UNIT) / SQUARES_PER_GAME_UNIT);
        int bottomRightY = ((int) (terrain.bottomRightCorner.y * SQUARES_PER_GAME_UNIT) / SQUARES_PER_GAME_UNIT);
        bottomRightPos = new Vector2(bottomRightX, bottomRightY);
    }

    private Terrain terrain;
    private Vector2 topLeftPos;
    private Vector2 bottomRightPos;
    public int SQUARES_PER_GAME_UNIT = -1; // How many squares will the map used by AStar pathfinding generate per

    public double[][] getMap(int squaresPerGameUnit) {
        checkSquares(squaresPerGameUnit);
        double[][] map = createEmptyMap();
        // We add half a tile, to get a height value at the center of each square

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (cannotGoHere(x, y)) {
                    map[y][x] = -1; // This value signifies an unpassable obstacle
                    continue;
                }
                map[y][x] = 1;
            }
        }
        return map;
    }

    private void checkForNullTerrain() {
        if (terrain == null) {
            throw new NullPointerException("Terrain was null");
        }
    }

    private void checkSquares(int squaresPerGameUnit) {
        if (squaresPerGameUnit <= 0) {
            throw new IndexOutOfBoundsException("squaresPerGameUnit was not in the correct range");
        }
        SQUARES_PER_GAME_UNIT = squaresPerGameUnit;
    }

    private double[][] createEmptyMap() {
        int xSquares = (int) getTerrainWidth() * SQUARES_PER_GAME_UNIT;
        int ySquares = (int) getTerrainHeight() * SQUARES_PER_GAME_UNIT;
        double[][] map = new double[ySquares][xSquares];
        return map;
    }

    private boolean cannotGoHere(int x, int y) {
        Vector2 positionInGameUnits = translateGridPositionIntoGameUnits(x, y);
        double value = terrain.getTerrainFunction().valueAt(positionInGameUnits);
        boolean tileIsObstacle = terrain.isPointInObstacle(positionInGameUnits);
        boolean cannotGoHere = value < 0 || tileIsObstacle;

        if (y == 120 && x == 34) {
            System.out.println("got it!");
        }
        return cannotGoHere;
    }

    /**
     * @return terrain width in game units
     */
    public double getTerrainWidth() {
        return Math.abs(bottomRightPos.x - topLeftPos.x);
    }

    /**
     * @return terrain height in game units
     */
    public double getTerrainHeight() {
        return Math.abs(bottomRightPos.y - topLeftPos.y);
    }

    private Vector2 translateGridPositionIntoGameUnits(double xPos, double yPos) {
        double newX = xPos / (double) SQUARES_PER_GAME_UNIT + topLeftPos.x;
        double newY = yPos / (double) SQUARES_PER_GAME_UNIT + topLeftPos.y;
        return new Vector2(newX, newY);
    }
}
