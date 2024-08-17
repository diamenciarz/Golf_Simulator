package bot.heuristics;

import java.util.ArrayList;

import datastorage.GameState;
import utility.math.Vector2;

public class ClosestEuclidianDistanceHeuristic implements Heuristic {

    @Override
    public double getShotValue(ArrayList<Vector2> shotPositions, GameState gameState) {
        double minDistance = Double.POSITIVE_INFINITY;
        Vector2 targetPosition = gameState.getTerrain().target.position;
        for (Vector2 position : shotPositions) {
            double distance = position.copy().translate(targetPosition.copy().scale(-1)).length();
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    @Override
    public boolean firstBetterThanSecond(double heuristic1, double heuristic2) {
        return heuristic1 < heuristic2;
    }
}
