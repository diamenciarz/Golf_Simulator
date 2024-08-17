package physics.solvers;

import datastorage.BallState;
import datastorage.Terrain;
import physics.PhysicsEngine;
import utility.math.Vector2;

public class RungeKutta2Solver implements IODESolver {
    private double h;

    public RungeKutta2Solver(double h) {
        setStepSize(h);
    }

    @Override
    public BallState calculateNewBallState(BallState state, Terrain terrain, PhysicsEngine engine) {
        Vector2 k1Velocity, k1Acceleration; BallState k1State;
        Vector2 k2Velocity, k2Acceleration; BallState k2State;

        k1State = state.copy();
        k1Velocity = k1State.velocity.copy();
        k1Acceleration = new Vector2(
                engine.xAcceleration(k1State, terrain),
                engine.yAcceleration(k1State, terrain)
        );

        k2State = state.copy();
        k2State.position.translate(k1Velocity.scaled(2*h/3));
        k2State.velocity.translate(k1Acceleration.scaled(2*h/3));
        k2Velocity = k2State.velocity;
        k2Acceleration = new Vector2(
                engine.xAcceleration(k2State, terrain),
                engine.yAcceleration(k2State, terrain)
        );

        BallState newState = state.copy();
        Vector2 positionUpdate = k1Velocity.translated(k2Velocity.scaled(3)).scaled(h/4);
        Vector2 velocityUpdate = k1Acceleration.translated(k2Acceleration.scaled(3)).scaled(h/4);
        newState.position.translate(positionUpdate);
        newState.velocity.translate(velocityUpdate);

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
        return "RK2";
    }
}
