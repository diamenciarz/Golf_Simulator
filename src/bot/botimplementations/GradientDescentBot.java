package bot.botimplementations;

import java.util.ArrayList;

import bot.heuristics.Heuristic;
import datastorage.Ball;
import datastorage.GameState;
import utility.math.Vector2;

public class GradientDescentBot implements IBot {
    private final Heuristic heuristic;
    private final double learningRate;
    private final IBot initialShotTaker;
    private int numSimulations;
    private int numIterations;

    public GradientDescentBot(Heuristic heuristic, double learningRate, IBot initialShotTaker) {
        this.heuristic = heuristic;
        this.learningRate = learningRate;
        this.initialShotTaker = initialShotTaker;
    }

    @Override
    public Vector2 findBestShot(GameState gameState) {
        numIterations = 0;
        numSimulations = 0;
        gameState = gameState.copy();
        Vector2 currentShot;
        // Take an initial shot
        if (initialShotTaker == null) {
            currentShot = new Vector2(Math.random() * 2 - 1, Math.random() * 2 - 1);
            currentShot.normalize().scale(Ball.maxSpeed * Math.random());
        } else {
            currentShot = initialShotTaker.findBestShot(gameState);
            numSimulations = initialShotTaker.getNumSimulations();
            numIterations = initialShotTaker.getNumIterations();
        }
        ArrayList<Vector2> positions = gameState.simulateShot(currentShot);
        double currentHeuristic = heuristic.getShotValue(positions, gameState);
        final double derivativeStep = 0.0001;
        Vector2 gradient;
        int numShots = 0;
        boolean holeInOne = false;
        // Check for hole in one
        holeInOne = positions.get(positions.size()-1).distanceTo(gameState.getTerrain().target.position) <= gameState.getTerrain().target.radius;
        while (numShots < 1000 && !holeInOne) {
            numIterations++;
            // Calculate the x partial derivative
            boolean xFlipped = false;
            Vector2 xShot = new Vector2(currentShot.x + derivativeStep, currentShot.y);
            if (xShot.length() > Ball.maxSpeed) {
                xFlipped = true;
                xShot = new Vector2(currentShot.x - derivativeStep, currentShot.y);
            }
            ArrayList<Vector2> positionsX = gameState.simulateShot(
                    xShot
            );
            numSimulations++;
            double newHeuristicX = heuristic.getShotValue(positionsX, gameState);
            double dx = (newHeuristicX - currentHeuristic)/derivativeStep;
            if (xFlipped) {
                dx = -dx;
            }
            // Calculate partial y derivative
            boolean yFlipped = false;
            Vector2 yShot = new Vector2(currentShot.x, currentShot.y + derivativeStep);
            if (yShot.length() > Ball.maxSpeed) {
                yFlipped = true;
                yShot = new Vector2(currentShot.x, currentShot.y - derivativeStep);
            }
            ArrayList<Vector2> positionsY = gameState.simulateShot(
                    yShot
            );
            numSimulations++;
            double newHeuristicY = heuristic.getShotValue(positionsY, gameState);
            double dy = (newHeuristicY - currentHeuristic)/derivativeStep;
            if (yFlipped) {
                dy = -dy;
            }
            // Calculate gradient
            gradient = new Vector2(dx, dy);
            // Determine ascent/descent
            int sign;
            if (heuristic.firstBetterThanSecond(newHeuristicX, currentHeuristic)) {
                if (newHeuristicX > currentHeuristic) {
                    sign = 1;
                } else {
                    sign = -1;
                }
            } else {
                if (newHeuristicX > currentHeuristic) {
                    sign = -1;
                } else {
                    sign = 1;
                }
            }
            currentShot.translate(gradient.scaled(sign * learningRate));
            // Clamp the velocity
            if (currentShot.length() > Ball.maxSpeed) {
                currentShot.normalize().scale(Ball.maxSpeed);
            }
            positions = gameState.simulateShot(currentShot);
            numSimulations++;
            currentHeuristic = heuristic.getShotValue(positions, gameState);
            holeInOne = positions.get(positions.size()-1).distanceTo(gameState.getTerrain().target.position) <= gameState.getTerrain().target.radius;
            numShots++;
        }

        return currentShot;
    }

    @Override
    public int getNumSimulations() {
        return numSimulations;
    }

    @Override
    public int getNumIterations() {
        return numIterations;
    }
}
