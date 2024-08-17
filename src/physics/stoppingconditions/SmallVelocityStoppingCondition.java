package physics.stoppingconditions;

import datastorage.BallState;

public class SmallVelocityStoppingCondition implements IStoppingCondition {

    @Override
    public boolean shouldStop(BallState newState, BallState previousState, double h) {
        return newState.velocity.length() < h;
    }

    @Override
    public String getConditionName() {
        return "smallV";
    }
    
}
