package physics.collisionsystems;

import datastorage.BallState;
import datastorage.Terrain;
import datastorage.obstacles.IObstacle;
import utility.math.Vector2;

public class StopCollisionSystem implements ICollisionSystem {

    @Override
    public BallState modifyStateDueToCollisions(BallState state, BallState previousState, double ballRadius,
            Terrain terrain) {
        BallState newState = state.copy();
        // Check if out of map
        if (terrain.isPositionCollidingWithMapBorder(newState)) {
            return getStoppedState(previousState);
        }
        // Check if inside an obstacle
        for (IObstacle obstacle : terrain.obstacles) {
            if (obstacle.isBallColliding(state.position, ballRadius)) {
                return getStoppedState(previousState);
            }
        }
        return newState;
    }

    private BallState getStoppedState(BallState previousState) {
        BallState stoppedState = previousState.copy();
        stoppedState.velocity = Vector2.zeroVector();
        return stoppedState;
    }

    @Override
    public String getName() {
        return "stop";
    }
}
