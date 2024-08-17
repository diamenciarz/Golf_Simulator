package physics;

import java.util.ArrayList;

import datastorage.*;
import physics.collisionsystems.ICollisionSystem;
import physics.solvers.IODESolver;
import physics.stoppingconditions.IStoppingCondition;
import utility.math.Vector2;

public class PhysicsEngine {

    public final double G = 9.81; // Gravitational constant

    public final IODESolver odeSolver;
    public final IStoppingCondition stoppingCondition;
    public final ICollisionSystem collisionSystem;

    /**
     * Constructor. Creates a new instance of the physics engine
     * 
     * @param odeSolver         The ODE solver to use
     * @param stoppingCondition The stopping condition to use
     * @param collisionSystem   The collision system to use
     */
    public PhysicsEngine(IODESolver odeSolver, IStoppingCondition stoppingCondition, ICollisionSystem collisionSystem) {
        this.odeSolver = odeSolver;
        this.stoppingCondition = stoppingCondition;
        this.collisionSystem = collisionSystem;
    }

    /**
     * Simulates a shot and stores the positions until the ball stops
     * 
     * @param initialSpeed The initial speed of the ball
     * @param ball         The ball to shoot
     * @param terrain      The terrain to shoot the ball on
     * @return ArrayList containing ball positions throughout the shot
     */
    public ArrayList<Vector2> simulateShot(Vector2 initialSpeed, Ball ball, Terrain terrain) {
        BallState tempState = ball.state.copy();
        ArrayList<Vector2> coordinates = new ArrayList<Vector2>();
        tempState.velocity = initialSpeed.copy();
        // Add the initial position
        coordinates.add(tempState.position.copy());

        while (tempState.velocity.length() != 0) {
            clampVelocity(tempState);
            BallState previousState = tempState.copy();
            tempState = odeSolver.calculateNewBallState(tempState, terrain, this);

            boolean shouldSetVelocityToZero = stoppingCondition.shouldStop(tempState, previousState,
                    odeSolver.getStepSize());
            if (shouldSetVelocityToZero) {
                handleStaticFriction(tempState, terrain);
            }
            tempState = collisionSystem.modifyStateDueToCollisions(tempState, previousState, ball.radius,
                    terrain);
            handleBallInWater(tempState, terrain);
            // Store the new position
            coordinates.add(tempState.position.copy());
        }

        return coordinates;
    }

    private void clampVelocity(BallState state) {
        if (state.velocity.length() > Ball.maxSpeed) {
            state.velocity.normalize().scale(Ball.maxSpeed);
        }
    }

    private void handleStaticFriction(BallState tempState, Terrain terrain) {
        Vector2 slope = calculateSlope(tempState.position, terrain);

        if (terrain.getStaticFriction(tempState.position) < slope.length()) {
            //If the static friction is smaller than the slope then set the velocity to the
            //slope
            tempState.velocity = slope.copy().reversed();
        } else {
            // Otherwise, make the velocity 0
            tempState.velocity = Vector2.zeroVector();
        }
    }

    /**
     * Set tempState's velocity to zero if ball is in water
     */
    private void handleBallInWater(BallState tempState, Terrain terrain) {
        if (tempState.getZCoordinate(terrain) < 0) {
            tempState.velocity = Vector2.zeroVector();
        }
    }

    private Vector2 calculateSlope(Vector2 position, Terrain terrain) {
        return new Vector2(terrain.xDerivativeAt(position), terrain.yDerivativeAt(position));
    }

    // region Accessor methods
    /**
     * Gets the x-acceleration
     * 
     * @param state   The ball state to calculate the acceleration for
     * @param terrain The terrain to calculate the acceleration on
     * @return The x-acceleration value
     */
    public double xAcceleration(BallState state, Terrain terrain) {
        double friction = terrain.getKineticFriction(state.position);
        Vector2 slope = new Vector2(
                terrain.xDerivativeAt(state.position),
                terrain.yDerivativeAt(state.position));
        double downHillForce = -G * slope.x;
        double frictionForce = G * friction * state.velocity.x / state.velocity.length();
        return (downHillForce - frictionForce);
    }

    /**
     * Gets the x-acceleration
     * 
     * @param state   The ball state to calculate the acceleration for
     * @param terrain The terrain to calculate the acceleration on
     * @return The x-acceleration value
     */
    public double yAcceleration(BallState state, Terrain terrain) {
        double friction = terrain.getKineticFriction(state.position);
        Vector2 slope = new Vector2(
                terrain.xDerivativeAt(state.position),
                terrain.yDerivativeAt(state.position));
        double downHillForce = -G * slope.y;
        double frictionForce = G * friction * state.velocity.y / state.velocity.length();
        return (downHillForce - frictionForce);
    }
    // endregion
}
