package bot;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import bot.heuristics.*;
import datastorage.*;
import physics.PhysicsEngine;
import physics.collisionsystems.StopCollisionSystem;
import physics.solvers.RungeKutta4Solver;
import physics.stoppingconditions.SmallVelocityStoppingCondition;
import utility.math.Vector2;

public class HeuristicPlotter {
    private Heuristic heuristic;
    private GameState gameState;

    public HeuristicPlotter(Heuristic heuristic, GameState gameState) {
        setHeuristic(heuristic);
        setGameState(gameState);
    }

    public void setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void generateTestData() {
        try {
            File f = new File("Phase 1/src/bot/results/"+heuristic.getClass().getName()+"-"+System.nanoTime()+".csv");
            FileWriter fw = new FileWriter(f);

            for (double vx=-Ball.maxSpeed; vx<=Ball.maxSpeed; vx+=0.1) {
                for (double vy=-Ball.maxSpeed; vy<=Ball.maxSpeed; vy+=0.1) {
                    if (Math.sqrt(vx*vx + vy*vy) <= Ball.maxSpeed) {
                        ArrayList<Vector2> shotPositions = gameState.simulateShot(new Vector2(vx, vy));
                        double heuristicVal = heuristic.getShotValue(shotPositions, gameState);
                        fw.append(vx + ", " + vy + ", " + heuristicVal + "\n");
                    } else {
                        fw.append(vx + ", " + vy + ", " + 0 + "\n");
                    }
                }
            }

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PhysicsEngine engine = new PhysicsEngine(
                new RungeKutta4Solver(0.01),
                new SmallVelocityStoppingCondition(),
                new StopCollisionSystem()
        );
        Terrain terrain = new Terrain(
                "0",
                0.2,
                0.08,
                new Vector2(-50, -50),
                new Vector2(50, 50)
        );
        terrain.target = new Target();
        terrain.target.setPosition(new Vector2(4, 1));
        terrain.target.setRadius(0.15);
        Ball ball = new Ball(new Vector2(-3, 0), Vector2.zeroVector());
        GameState gameState = new GameState(terrain, ball, engine);

        HeuristicPlotter hp = new HeuristicPlotter(new FinalAStarDistanceHeuristic(terrain), gameState);
        hp.generateTestData();
    }
}
