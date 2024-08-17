package physics.stoppingconditions;

import datastorage.BallState;

public interface IStoppingCondition {
    public boolean shouldStop(BallState newState, BallState previousState, double h);

    public String getConditionName();
}
