package physics;

import datastorage.GameState;
import physics.collisionsystems.BounceCollisionSystem;
import physics.solvers.RungeKutta4Solver;
import physics.stoppingconditions.SmallVelocityStoppingCondition;
import reader.GameStateLoader;
import utility.math.Vector2;

import java.util.ArrayList;

public class ShotSimulator {
    public static void main(String[] args) {
        GameState game = GameStateLoader.readFile();

        PhysicsEngine pe = new PhysicsEngine(
                new RungeKutta4Solver(0.01),
                new SmallVelocityStoppingCondition(),
                new BounceCollisionSystem()
        );

        ArrayList<Vector2> positions = pe.simulateShot(new Vector2(4, 0), game.getBall(), game.getTerrain());

        boolean found = false;
        Vector2 previousPosition = positions.get(0);
        for (Vector2 position : positions) {
            if (position.x < previousPosition.x) {
                System.out.println(previousPosition);
                System.out.println(position);
                found = true;
                break;
            }
            previousPosition = position;
        }
        if (!found) {
            System.out.println(positions.get(positions.size()-1));
        }

    }
}
