package bot.botimplementations;

import java.util.ArrayList;
import java.util.Random;

import bot.heuristics.Heuristic;
import datastorage.Ball;
import datastorage.GameState;
import utility.math.Vector2;

public class ParticleSwarmBot implements IBot {

    private final Heuristic heuristic;
    private final double w, c1, c2;
    private final Random random;
    private final int numParticles, numGenerations;
    public int numSimulations, numIterations;

    public ParticleSwarmBot(Heuristic heuristic, double w, double c1, double c2, int numParticles, int numGenerations) {
        this.heuristic = heuristic;
        this.w = w;
        this.c1 = c1;
        this.c2 = c2;
        this.random = new Random();
        this.numParticles = numParticles;
        this.numGenerations = numGenerations;
    }

    @Override
    public Vector2 findBestShot(GameState gameState) {
        numSimulations = 0;
        numIterations = 0;
        gameState = gameState.copy();
        Vector2 bestShot = null;
        double bestHeuristic = 0;
        // Initialize the population
        Particle[] particles = new Particle[numParticles];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(gameState);
        }
        // Find the best shot
        for (Particle particle : particles) {
            if (bestShot == null || heuristic.firstBetterThanSecond(particle.bestHeuristicValue, bestHeuristic)) {
                bestShot = particle.bestPosition.copy();
                bestHeuristic = particle.bestHeuristicValue;
            }
        }
        for (int generation = 1; generation <= numGenerations; generation++) {
            numIterations++;
            for (Particle particle : particles) {
                particle.move(bestShot);
            }
            // Find the best shot
            for (Particle particle : particles) {
                if (heuristic.firstBetterThanSecond(particle.bestHeuristicValue, bestHeuristic)) {
                    bestShot = particle.bestPosition.copy();
                    bestHeuristic = particle.bestHeuristicValue;
                    System.out.println("New best: " + bestHeuristic);
                }
            }
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

    private class Particle {
        Vector2 velocity;
        Vector2 position;
        Vector2 bestPosition;
        double bestHeuristicValue;
        GameState gameState;

        Particle(GameState gameState) {
            this.gameState = gameState;
            position = new Vector2(
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1).normalize().scale(random.nextDouble() * Ball.maxSpeed);
            velocity = new Vector2(
                    random.nextDouble() * 2 - 1,
                    random.nextDouble() * 2 - 1).normalize();
            updateBestPosition();
        }

        void updateBestPosition() {
            ArrayList<Vector2> ballPositions = gameState.simulateShot(position);
            numSimulations++;
            double heuristicVal = heuristic.getShotValue(ballPositions, gameState);
            if (bestPosition == null || heuristic.firstBetterThanSecond(heuristicVal, bestHeuristicValue)) {
                bestPosition = position.copy();
                bestHeuristicValue = heuristicVal;
            }
        }

        void move(Vector2 globalBest) {
            // Move
            position.translate(velocity);
            if (position.length() > Ball.maxSpeed) {
                position.normalize().scale(Ball.maxSpeed);
            }
            // Update velocity
            velocity.translate(velocity.scaled(w));
            velocity.translate(bestPosition.translated(position.reversed()).scaled(c1 * random.nextDouble()));
            velocity.translate(globalBest.translated(position.reversed()).scaled(c2 * random.nextDouble()));

            updateBestPosition();
        }
    }
}
