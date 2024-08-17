package physics.solvers;

import datastorage.BallState;
import datastorage.Terrain;
import physics.PhysicsEngine;

public interface IODESolver {
    public BallState calculateNewBallState(BallState state, Terrain terrain, PhysicsEngine engine);
    public double getStepSize();
    public void setStepSize(double h);
    public String getSolverName();
}
