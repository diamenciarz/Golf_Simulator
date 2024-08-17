package bot.heuristics;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import bot.AStar;
import datastorage.GameState;
import datastorage.Terrain;
import utility.math.Vector2;

public class FinalAStarDistanceHeuristic implements Heuristic {

    AStar aStar;
    private final Heuristic distanceHeuristic = new FinalEuclidianDistanceHeuristic();
    private boolean useEuclideanDistance = false;

    public FinalAStarDistanceHeuristic(Terrain terrain) {
        aStar = new AStar(terrain);
    }

    @Override
    public double getShotValue(ArrayList<Vector2> shotPositions, GameState gameState) {
        useEuclideanDistance = false;
        trySwitchMode(shotPositions, gameState);
        if (useEuclideanDistance) {
            return countEuclideanDistance(shotPositions, gameState);
        }
        // Otherwise, check AStar
        Vector2 finalPosition = shotPositions.get(shotPositions.size() - 1);
        try {
            return aStar.getDistanceToTarget(finalPosition, 2);
        } catch (NoSuchAlgorithmException e) {
            return 1000;
        }
    }

    private void trySwitchMode(ArrayList<Vector2> shotPositions, GameState gameState) {
        double lastPointDistance = distanceHeuristic.getShotValue(shotPositions, gameState);
        // There could be no need to check the distance with AStar
        double targetRadius = gameState.getTerrain().target.radius;
        boolean ballPassedThroughTarget = lastPointDistance < targetRadius;
        if (ballPassedThroughTarget) {
            System.out.println("Switched mode by radius: " + targetRadius);
            useEuclideanDistance = true;
        }
    }

    private double countEuclideanDistance(ArrayList<Vector2> shotPositions, GameState gameState) {
        return distanceHeuristic.getShotValue(shotPositions, gameState);
    }

    @Override
    public boolean firstBetterThanSecond(double heuristic1, double heuristic2) {
        return heuristic1 < heuristic2;
    }
}
