package bot.heuristics;

import java.util.ArrayList;

import datastorage.GameState;
import utility.math.Vector2;

public interface Heuristic {
    public double getShotValue(ArrayList<Vector2> shotPositions, GameState gameState);
    public boolean firstBetterThanSecond(double heuristic1, double heuristic2);
}
