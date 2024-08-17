package bot.botimplementations;

import java.util.ArrayList;

import bot.heuristics.Heuristic;
import datastorage.Ball;
import datastorage.GameState;
import utility.math.Vector2;

public class HillClimbingBot implements IBot {

    private final Heuristic heuristic;
    private final double learningRate;
    private final int numNeighbours;
    private final IBot initialShotTaker;
    private int numSimulations;
    private int numIterations;

    public HillClimbingBot(Heuristic heuristic, double learningRate, int numNeighbours, IBot initialShotTaker) {
        this.heuristic = heuristic;
        this.learningRate = learningRate;
        this.numNeighbours = numNeighbours;
        this.initialShotTaker = initialShotTaker;
    }

    @Override
    public Vector2 findBestShot(GameState gameState) {
        numIterations = 0;
        numSimulations = 0;
        gameState = gameState.copy();
        // Take an initial shot
        Vector2 bestShot;
        if (initialShotTaker == null) {
            bestShot = new Vector2(Math.random() * 2 - 1, Math.random() * 2 - 1);
            bestShot.normalize().scale(Math.random()*Ball.maxSpeed);
        } else {
            bestShot = initialShotTaker.findBestShot(gameState);
            numSimulations = initialShotTaker.getNumSimulations();
            numIterations = initialShotTaker.getNumIterations();
        }

        boolean bestShotUpdated = true;

        // Initial heuristic calculation
        ArrayList<Vector2> positions = gameState.simulateShot(bestShot);
        double bestHeuristicVal = heuristic.getShotValue(positions, gameState);
        numSimulations++;

        //boolean holeInOne = positions.get(positions.size()-1).distanceTo(gameState.getTerrain().target.position) <= gameState.getTerrain().target.radius;

        while (bestShotUpdated) {
            numIterations++;

            bestShotUpdated = false;
            Vector2 tempBestShot = bestShot.copy();

            for (int neighbour = 0; neighbour < numNeighbours; neighbour++) {

                double degree = 360.0*neighbour/numNeighbours;

                Vector2 changeVector = new Vector2(Math.cos(degree * Math.PI / 180), Math.sin(degree * Math.PI / 180)).scale(learningRate);
                Vector2 velocity = bestShot.translated(changeVector);

                if (velocity.length() > Ball.maxSpeed) {
                    velocity.normalize().scale(Ball.maxSpeed);
                }

                positions = gameState.simulateShot(velocity);
                numSimulations++;

                //if (!holeInOne)
                //    holeInOne = positions.get(positions.size()-1).distanceTo(gameState.getTerrain().target.position) <= gameState.getTerrain().target.radius;

                double heuristicVal = heuristic.getShotValue(positions, gameState);

                if (heuristic.firstBetterThanSecond(heuristicVal, bestHeuristicVal)) {
                    tempBestShot = velocity.copy();
                    bestHeuristicVal = heuristicVal;
                    bestShotUpdated = true;
                }
            }

            bestShot = tempBestShot;
        }
        return bestShot;
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
