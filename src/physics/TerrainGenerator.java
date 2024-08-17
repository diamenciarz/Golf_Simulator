package physics;

import datastorage.*;

public class TerrainGenerator {

    private static double step = 0.05;
    private static int xRepetitions = 100;
    private static int yRepetitions = 100;

    public static void generateMeshGrid(Terrain terrain) {
        double[][] meshGrid = new double[xRepetitions][yRepetitions];

        for (int x = 0; x < xRepetitions; x++) {
            for (int y = 0; y < yRepetitions; y++) {

                double xCoord = x * step;
                double yCoord = y * step;
                meshGrid[x][y] = terrain.getTerrainFunction().valueAt(xCoord, yCoord);
            }
        }
        terrain.meshGrid2 = meshGrid;
    }
}
