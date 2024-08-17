package reader;

import java.util.ArrayList;

import datastorage.Terrain;
import datastorage.obstacles.IObstacle;
import datastorage.obstacles.ObstacleBox;
import utility.math.Vector2;

public class MazeMapGenerator {
    private static final double staticFriction = 0.3d;
    private static final double kineticFriction = 0.2d;
    private static final Vector2 startingCorner = Vector2.zeroVector();
    private static final String defaultFunction = "0";

    private static final double GAME_UNITS_PER_SQUARE = 2;

    public static void main(String[] args) {
        boolean[][] grid = { { true, true }, { false, false }, { true, false } };
        Terrain terrain = getMazeTerrain(grid, new Vector2(8, 3));
        System.out.println(terrain);
    }

    /**
     * @param mazeGrid a rectuangular grid with {@code true} signifying path and
     *                 {@code false} values signifying obstacles
     * @return a Terrain object with obstacles that form a maze specified in the
     *         {@code mazeGrid}
     */
    public static Terrain getMazeTerrain(boolean[][] mazeGrid, Vector2 mapSize) {
        Terrain terrain = new Terrain(defaultFunction, staticFriction, kineticFriction, startingCorner,
                countLimitingCorner(mazeGrid, mapSize));
        terrain.obstacles = (ArrayList<IObstacle>) createObstacles(mazeGrid);
        return terrain;
    }

    private static Vector2 countLimitingCorner(boolean[][] mazeGrid, Vector2 mapSize) {
        double height = mazeGrid.length * GAME_UNITS_PER_SQUARE;
        double width = mazeGrid[0].length * GAME_UNITS_PER_SQUARE;
        Vector2 limitingCorner = new Vector2(width, height);
        if (limitingCorner.x < mapSize.x) {
            limitingCorner.x = mapSize.x;
        }
        if (limitingCorner.y < mapSize.y) {
            limitingCorner.y = mapSize.y;
        }
        return limitingCorner;
    }

    private static ArrayList<IObstacle> createObstacles(boolean[][] mazeGrid) {
        ArrayList<IObstacle> obstacles = new ArrayList<IObstacle>();
        for (int y = 0; y < mazeGrid.length; y++) {
            for (int x = 0; x < mazeGrid[y].length; x++) {
                handleTile(x, y, mazeGrid, obstacles);
            }
        }
        return obstacles;
    }

    private static void handleTile(int x, int y, boolean[][] mazeGrid, ArrayList<IObstacle> obstacles) {
        boolean tileIsObstacle = mazeGrid[y][x] == false;
        if (tileIsObstacle) {
            IObstacle obstacle = createObstacle(x, y);
            obstacles.add(obstacle);
        }
    }

    private static IObstacle createObstacle(int x, int y) {
        Vector2 bottomLeftPosition = translateGridPositionToGameUnits(x, y);
        Vector2 topRightPosition = translateGridPositionToGameUnits(x + 1, y + 1);
        return new ObstacleBox(bottomLeftPosition, topRightPosition);

    }

    private static Vector2 translateGridPositionToGameUnits(int x, int y) {
        return new Vector2(startingCorner.x + x * GAME_UNITS_PER_SQUARE,
                startingCorner.y + y * GAME_UNITS_PER_SQUARE);
    }
}
