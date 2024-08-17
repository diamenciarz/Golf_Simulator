package physics;

import datastorage.BallState;
import datastorage.Terrain;
import physics.collisionsystems.ICollisionSystem;
import physics.solvers.IODESolver;
import physics.stoppingconditions.IStoppingCondition;
import utility.math.Vector2;

public class PhysicsEngine2 extends PhysicsEngine {
    /**
     * Constructor. Creates a new instance of the physics engine
     *
     * @param odeSolver         The ODE solver to use
     * @param stoppingCondition The stopping condition to use
     * @param collisionSystem   The collision system to use
     */
    public PhysicsEngine2(IODESolver odeSolver, IStoppingCondition stoppingCondition, ICollisionSystem collisionSystem) {
        super(odeSolver, stoppingCondition, collisionSystem);
    }

    /**
     * Gets the x-acceleration
     *
     * @param state   The ball state to calculate the acceleration for
     * @param terrain The terrain to calculate the acceleration on
     * @return The x-acceleration value
     */
    @Override
    public double xAcceleration(BallState state, Terrain terrain) {
        double friction = terrain.getKineticFriction(state.position);
        Vector2 slope = new Vector2(
                terrain.xDerivativeAt(state.position),
                terrain.yDerivativeAt(state.position));
        double downHillForce = -G * slope.x/(1+slope.x*slope.x + slope.y*slope.y);
        double frictionForce1 = G * friction/Math.sqrt(1+slope.x*slope.x + slope.y*slope.y);
        double slopeTerm = slope.x*state.velocity.x + slope.y*state.velocity.y;
        double frictionForce2 = state.velocity.x/Math.sqrt(state.velocity.x*state.velocity.x + state.velocity.y*state.velocity.y + slopeTerm*slopeTerm);
        return downHillForce - frictionForce1*frictionForce2;
    }

    /**
     * Gets the x-acceleration
     *
     * @param state   The ball state to calculate the acceleration for
     * @param terrain The terrain to calculate the acceleration on
     * @return The x-acceleration value
     */
    @Override
    public double yAcceleration(BallState state, Terrain terrain) {
        double friction = terrain.getKineticFriction(state.position);
        Vector2 slope = new Vector2(
                terrain.xDerivativeAt(state.position),
                terrain.yDerivativeAt(state.position));
        double downHillForce = -G * slope.y/(1+slope.x*slope.x + slope.y*slope.y);
        double frictionForce1 = G * friction/Math.sqrt(1+slope.x*slope.x + slope.y*slope.y);
        double slopeTerm = slope.x*state.velocity.x + slope.y*state.velocity.y;
        double frictionForce2 = state.velocity.y/Math.sqrt(state.velocity.x*state.velocity.x + state.velocity.y*state.velocity.y + slopeTerm*slopeTerm);
        return downHillForce - frictionForce1*frictionForce2;
    }
}
