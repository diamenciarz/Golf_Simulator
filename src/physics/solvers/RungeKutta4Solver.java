package physics.solvers;

import datastorage.BallState;
import datastorage.Terrain;
import physics.PhysicsEngine;
import utility.math.Vector2;

public class RungeKutta4Solver implements IODESolver {
    private double h;

    public RungeKutta4Solver(double h) {
        setStepSize(h);
    }

    @Override
    public BallState calculateNewBallState(BallState state, Terrain terrain, PhysicsEngine engine) {
        Vector2 k1Velocity, k1Acceleration; BallState k1State;
        Vector2 k2Velocity, k2Acceleration; BallState k2State;
        Vector2 k3Velocity, k3Acceleration; BallState k3State;
        Vector2 k4Velocity, k4Acceleration; BallState k4State;

        k1State = state.copy();
        k1Velocity = k1State.velocity.copy();
        k1Acceleration = new Vector2(
                engine.xAcceleration(k1State, terrain),
                engine.yAcceleration(k1State, terrain)
        );

        k2State = state.copy();
        k2State.position.translate(k1Velocity.scaled(h/2));
        k2State.velocity.translate(k1Acceleration.scaled(h/2));
        k2Velocity = k2State.velocity;
        k2Acceleration = new Vector2(
                engine.xAcceleration(k2State, terrain),
                engine.yAcceleration(k2State, terrain)
        );

        k3State = state.copy();
        k3State.position.translate(k2Velocity.scaled(h/2));
        k3State.velocity.translate(k2Acceleration.scaled(h/2));
        k3Velocity = k3State.velocity;
        k3Acceleration = new Vector2(
                engine.xAcceleration(k3State, terrain),
                engine.yAcceleration(k3State, terrain)
        );

        k4State = state.copy();
        k4State.position.translate(k3Velocity.scaled(h));
        k4State.velocity.translate(k3Acceleration.scaled(h));
        k4Velocity = k4State.velocity;
        k4Acceleration = new Vector2(
                engine.xAcceleration(k4State, terrain),
                engine.yAcceleration(k4State, terrain)
        );

        BallState newState = state.copy();
        Vector2 positionUpdate = k1Velocity.translated(k2Velocity.scaled(2)).translated(k3Velocity.scaled(2)).translated(k4Velocity).scaled(h/6);
        Vector2 velocityUpdate = k1Acceleration.translated(k2Acceleration.scaled(2)).translated(k3Acceleration.scaled(2)).translated(k4Acceleration).scaled(h/6);
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
        // TODO Auto-generated method stub
        return "RK4";
    }
}
