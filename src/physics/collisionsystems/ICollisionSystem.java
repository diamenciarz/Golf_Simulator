package physics.collisionsystems;

import datastorage.*;

public interface ICollisionSystem {

    public BallState modifyStateDueToCollisions(BallState state, BallState previousState, double ballRadius, Terrain terrain);

    public String getName();
}
