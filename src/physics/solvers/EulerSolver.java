package physics.solvers;

import datastorage.BallState;
import datastorage.Terrain;
import physics.PhysicsEngine;
import utility.math.Vector2;

public class EulerSolver implements IODESolver {

    private double h; // The step size to use

    public EulerSolver(double h) {
        setStepSize(h);
    }

    @Override
    public BallState calculateNewBallState(BallState state, Terrain terrain, PhysicsEngine engine) {
        BallState newState = state.copy();
        // Update the position
        newState.position.translate(state.velocity.scaled(h));
        // Update the velocity
        // Calculate the acceleration
        Vector2 acceleration = new Vector2(
                engine.xAcceleration(state, terrain),
                engine.yAcceleration(state, terrain)
        );
        newState.velocity.translate(acceleration.scaled(h));
        return newState;
    }

    @Override
    public double getStepSize() {
        return h;
    }

    @Override
    public void setStepSize(double h) {
        this.h = h;
    }

    @Override
    public String getSolverName() {
        return "Euler";
    }

}
