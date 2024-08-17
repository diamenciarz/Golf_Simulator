package bot.heuristics;

import datastorage.GameState;
import utility.math.Vector2;

import java.util.ArrayList;

public class FinalClosestEuclidianDistanceHeuristic implements Heuristic {

    private final Heuristic finalHeuristic = new FinalEuclidianDistanceHeuristic();
    private final Heuristic closestHeuristic = new ClosestEuclidianDistanceHeuristic();

    @Override
    public double getShotValue(ArrayList<Vector2> shotPositions, GameState gameState) {
        return finalHeuristic.getShotValue(shotPositions, gameState) + closestHeuristic.getShotValue(shotPositions, gameState);
    }

    @Override
    public boolean firstBetterThanSecond(double heuristic1, double heuristic2) {
        return heuristic1 < heuristic2;
    }
}
