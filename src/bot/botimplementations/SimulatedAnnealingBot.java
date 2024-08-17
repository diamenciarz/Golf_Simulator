package bot.botimplementations;

import bot.heuristics.Heuristic;
import datastorage.Ball;
import datastorage.GameState;
import utility.math.Vector2;

import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealingBot implements IBot {
    private final Heuristic heuristic;
    private final double learningRate;
    private final int numIterations;
    private final IBot initialShotTaker;
    private int numSimulations;
    private int numIterations2;

    public SimulatedAnnealingBot(Heuristic heuristic, double learningRate, int numIterations, IBot initialShotTaker) {
        this.heuristic = heuristic;
        this.learningRate = learningRate;
        this.numIterations = numIterations;
        this.initialShotTaker = initialShotTaker;
    }

    @Override
    public Vector2 findBestShot(GameState gameState) {
        numIterations2 = 0;
        numSimulations = 0;
        gameState = gameState.copy();

        Random random = new Random();

        Vector2 shot;
        if (initialShotTaker == null) {
            shot = new Vector2(Math.random() * 2 - 1, Math.random() * 2 - 1);
            shot.normalize().scale(Math.random()* Ball.maxSpeed);
        } else {
            shot = initialShotTaker.findBestShot(gameState);
            numSimulations = initialShotTaker.getNumSimulations();
            numIterations2 = initialShotTaker.getNumIterations();
        }

        ArrayList<Vector2> positions = gameState.simulateShot(shot);
        double currentHeuristicVal = heuristic.getShotValue(
                positions,
                gameState
        );

        boolean holeInOne = false;
        holeInOne = positions.get(positions.size()-1).distanceTo(gameState.getTerrain().target.position) <= gameState.getTerrain().target.radius;

        for (int i=0; i<numIterations && !holeInOne; i++) {
            numIterations2++;

            double temperature = 1 - (double) (i+1)/numIterations;

            // Select random neighbour
            double degree = random.nextDouble()*360;

            Vector2 updateVector = new Vector2(
                    Math.cos(degree * Math.PI / 180),
                    Math.sin(degree * Math.PI / 180)
            ).scale(learningRate);

            Vector2 neighbourShot = shot.translated(updateVector);
            if (neighbourShot.length() > Ball.maxSpeed) {
                neighbourShot.normalize().scale(Ball.maxSpeed);
            }

            positions = gameState.simulateShot(neighbourShot);
            double heuristicVal = heuristic.getShotValue(
                    positions,
                    gameState
            );
            numSimulations++;

            holeInOne = positions.get(positions.size()-1).distanceTo(gameState.getTerrain().target.position) <= gameState.getTerrain().target.radius;

            double selectProbability; // Calculate the probability of selecting this neighbour

            if (heuristic.firstBetterThanSecond(heuristicVal, currentHeuristicVal)) {
                selectProbability = 1;
            } else {
                selectProbability = Math.exp(-Math.abs(heuristicVal - currentHeuristicVal)/temperature);
            }

            if (selectProbability > random.nextDouble()) {
                shot = neighbourShot.copy();
                currentHeuristicVal = heuristicVal;
            }
        }

        return shot;
    }

    @Override
    public int getNumSimulations() {
        return numSimulations;
    }

    @Override
    public int getNumIterations() {
        return numIterations2;
    }
}
