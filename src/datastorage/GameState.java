package datastorage;

import physics.PhysicsEngine;
import utility.math.Vector2;

import java.util.ArrayList;

public class GameState {
    private final Terrain terrain;
    private final PhysicsEngine physicsEngine;
    private final Ball ball;

    public GameState(Terrain terrain, Ball ball, PhysicsEngine physicsEngine) {
        this.terrain = terrain;
        this.ball = ball;
        this.physicsEngine = physicsEngine;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public PhysicsEngine getPhysicsEngine() {
        return physicsEngine;
    }

    public Ball getBall() {
        return ball;
    }

    public GameState copy() {
        return new GameState(terrain, ball.copy(), physicsEngine);
    }

    public void setBallPosition(Vector2 position) {
        ball.state.position = position.copy();
    }

    public void setBallVelocity(Vector2 velocity) {
        ball.state.velocity = velocity.copy();
    }

    public ArrayList<Vector2> simulateShot(Vector2 velocity) {
        return physicsEngine.simulateShot(velocity, ball, terrain);
    }
}
