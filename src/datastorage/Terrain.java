package datastorage;

import datastorage.obstacles.IObstacle;
// import org.mariuszgromada.math.mxparser.Expression;
import datastorage.obstacles.ObstacleBox;
import datastorage.obstacles.ObstacleTree;
import datastorage.obstacles.ObstacleWall;
import function.Function;
import utility.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;

public class Terrain {
    // This is generated after Terrain is created
    public double[][] meshGrid2;
    public float[] meshGrid;

    public float[] heightmap;
    public float[] reversedHeightmap;

    // All of the data below should be included, when Terrain is created
    public Zone[] zones = new Zone[0];
    public ArrayList<IObstacle> obstacles;
    public Target target;
    public Vector2 ballStartingPosition;

    public float minScaledVal = Integer.MAX_VALUE;
    public float maxScaledVal = Integer.MIN_VALUE;

    // The corners of the whole map. The function is evaluated in this rectangle
    public Vector2 topLeftCorner;
    public Vector2 bottomRightCorner;

    public double staticFriction;
    public double kineticFriction;

    public TerrainHeightFunction terrainFunction;

    public float minVal = -10;
    public float maxVal = 10;
    public double xOff;
    public double yOff;

    public Terrain() {
    }

    public void addObstacle(IObstacle obstacle){
        obstacles.add(obstacle);
    }
    public void removeObstacleAt(int id, String obstacleType){
        if(obstacleType.equals("Box")){
            obstacles.removeIf(i -> i.getId() == id && i instanceof ObstacleBox);
        }
        if(obstacleType.equals("Tree")){
            obstacles.removeIf(i -> i.getId() == id && i instanceof ObstacleTree);
        }
        if(obstacleType.equals("Wall")){
            obstacles.removeIf(i -> i.getId() == id && i instanceof ObstacleWall);
        }
        System.out.println(obstacles.size());
    }

    // This value seems to be the right number, so no need to provide it as input
    // everytime.
    private final int VERTECES_PER_SIDE = 1025;

    public int getVERTECES_PER_SIDE() {
        return VERTECES_PER_SIDE;
    }

    public final double NORMAL_FACTOR = 50;

    public Terrain(String function, double staticFriction, double kineticFriction, Vector2 startingCorner,
            Vector2 limitingCorner) {
        this.terrainFunction = new TerrainHeightFunction(function);
        this.staticFriction = staticFriction;
        this.kineticFriction = kineticFriction;
        this.topLeftCorner = startingCorner;
        this.bottomRightCorner = limitingCorner;
        calculateHeightMap(VERTECES_PER_SIDE);
    }

    public boolean isPointInZone(double x, double y) {
        for (Zone z : zones) {
            if (z.isPositionInside(new Vector2(x, y))) {
                return true;
            }
        }
        return false;
    }

    public boolean isPointInObstacle(Vector2 point) {
        for (IObstacle obstacle : obstacles) {
            if (obstacle.isPositionColliding(point)) {
                return true;
            }
        }
        return false;
    }

    public void addZone(Vector2 bottomLeft, Vector2 topRight, double zoneStaticFriction, double zoneKineticFriction) {
        Zone[] temp = new Zone[zones.length + 1];
        for (int i = 0; i < zones.length; i++) {
            temp[i] = zones[i];
        }
        Zone z = new Zone(bottomLeft.copy(), topRight.copy());
        z.staticFriction = zoneStaticFriction;
        z.kineticFriction = zoneKineticFriction;
        temp[temp.length - 1] = z;
        zones = temp;
    }

    public boolean isValid(int accuracy) {
        double xStep = (bottomRightCorner.x - topLeftCorner.x) / accuracy;
        double yStep = (bottomRightCorner.y - topLeftCorner.y) / accuracy;

        Function dfxx = terrainFunction.dfx.getDerivative("x");
        Function dfyy = terrainFunction.dfy.getDerivative("y");
        Function dfxy = terrainFunction.dfx.getDerivative("y");

        for (int xx = 0; xx < accuracy; xx++) {
            for (int yy = 0; yy < accuracy; yy++) {
                double x = topLeftCorner.x + xx * xStep;
                double y = topLeftCorner.y + yy * yStep;

                double fVal = Math.abs(terrainFunction.valueAt(x, y));
                double dfxVal = Math.abs(terrainFunction.xDerivativeAt(x, y));
                double dfyVal = Math.abs(terrainFunction.yDerivativeAt(x, y));
                double dfxxVal = Math.abs(dfxx.evaluate(new String[] { "x", "y" }, new double[] { x, y }));
                double dfyyVal = Math.abs(dfyy.evaluate(new String[] { "x", "y" }, new double[] { x, y }));
                double dfxyVal = Math.abs(dfxy.evaluate(new String[] { "x", "y" }, new double[] { x, y }));

                if (fVal > 10 || dfxVal > 0.15 || dfyVal > 0.15 ||
                        dfxxVal > 0.1 || dfyyVal > 0.1 || dfxyVal > 0.1) {
                    return false;
                }
            }
        }

        return true;
    }

    public double xDerivativeAt(Vector2 position) {
        double functionVal = terrainFunction.valueAt(position.x, position.y);
        if (functionVal > maxVal || functionVal < minVal) {
            return 0;
        } else {
            return terrainFunction.xDerivativeAt(position.x, position.y);
        }
    }

    public double yDerivativeAt(Vector2 position) {
        double functionVal = terrainFunction.valueAt(position.x, position.y);
        if (functionVal > maxVal || functionVal < minVal) {
            return 0;
        } else {
            return terrainFunction.yDerivativeAt(position.x, position.y);
        }
    }

    public double getStaticFriction(Vector2 position) {
        for (Zone zone : zones) {
            if (zone.isPositionInside(position)) {
                return zone.staticFriction;
            }
        }
        return staticFriction;
    }

    public double getKineticFriction(Vector2 position) {
        for (Zone zone : zones) {
            if (zone.isPositionInside(position)) {
                return zone.kineticFriction;
            }
        }
        return kineticFriction;
    }

    /**
     * Recalculates the heightmap that is used for
     * @param numVerteces
     */
    private void calculateHeightMap(int numVerteces) {
        heightmap = new float[numVerteces * numVerteces];
        int pos = 0;
        this.xOff = (bottomRightCorner.x - topLeftCorner.x) / numVerteces;
        this.yOff = (bottomRightCorner.y - topLeftCorner.y) / numVerteces;
        for (int x = 0; x < numVerteces; x++) {
            for (int y = 0; y < numVerteces; y++) {
                double xx = topLeftCorner.x + x * this.xOff;
                double yy = topLeftCorner.y + y * this.yOff;
                float val = (float) terrainFunction.valueAt(xx, yy);
                if (val > maxVal) {
                    maxVal = val;
                }
                if (val < minVal) {
                    minVal = val;
                }
                heightmap[pos] = val;
                pos++;
            }
        }
        for (int i = 0; i < heightmap.length; i++) {
            float val = heightmap[i];
            val = normalizeHeight(val);
            if (val > maxScaledVal) {
                maxScaledVal = val;
            }
            if (val < minScaledVal) {
                minScaledVal = val;
            }
            heightmap[i] = val;
        }
        flipAxis();
    }

    private void flipAxis(){
        reversedHeightmap = new float[VERTECES_PER_SIDE * VERTECES_PER_SIDE];
        for(int y=0; y<VERTECES_PER_SIDE;y++){
            for(int x=0; x<VERTECES_PER_SIDE; x++){
                reversedHeightmap[y*VERTECES_PER_SIDE+x]= heightmap[x*VERTECES_PER_SIDE+y];
            }
        }
    }

    public float heightMapValueAt(Vector2 targetPos){
        float val = (float) getTerrainFunction().valueAt(targetPos.x,targetPos.y);
        val = normalizeHeight(val);
        return val;
    }

    private float normalizeHeight(float val) {
        val += Math.abs(minVal);
        val /= maxVal - minVal;
        if (val < 0) {
            val = 0;
        }
        if (val > 1) {
            val = 1;
        }
        val *= NORMAL_FACTOR;
        return val;
    }

    public boolean isPositionCollidingWithMapBorder(BallState state) {
        boolean isOutOfMap = state.position.x > bottomRightCorner.x || state.position.x < topLeftCorner.x
                || state.position.y > bottomRightCorner.y || state.position.y < topLeftCorner.y;
        if (isOutOfMap) {
            return true;
        }
        return false;
    }

    public void print() {
        System.out.println("Mesh grid:");
        System.out.print("Starting position: ");
        System.out.println(ballStartingPosition);
        System.out.print("Starting corner: ");
        System.out.println(topLeftCorner);
        System.out.print("Limiting corner: ");
        System.out.println(bottomRightCorner);
        System.out.print("Kinetic friction: ");
        System.out.println(kineticFriction);
        System.out.print("Static friction: ");
        System.out.println(staticFriction);

        //Print.printSquare(meshGrid);
        for (Zone zone : zones) {
            zone.print();
        }
        for (IObstacle obstacle : obstacles) {
            obstacle.print();
        }
        target.print();
    }

    public void setTerrainFunction(TerrainHeightFunction terrainFunction) {
        this.terrainFunction = terrainFunction;
        calculateHeightMap(VERTECES_PER_SIDE);
    }

    public TerrainHeightFunction getTerrainFunction(){
        return terrainFunction;
    }

}