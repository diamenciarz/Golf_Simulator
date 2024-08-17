package physics.stoppingconditions;

import datastorage.BallState;
import utility.math.Vector2;

public class DotProductSmallVelocityStoppingCondition implements IStoppingCondition {
    @Override
    public boolean shouldStop(BallState newState, BallState previousState, double h) {
        return newState.velocity.length() < h || Vector2.dotProduct(newState.velocity, previousState.velocity) < 0;
    }

    @Override
    public String getConditionName() {
        return "dotProductSmallV";
    }
}
