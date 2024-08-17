package datastorage;

import utility.math.Vector2;

public class Ball {
    public BallState state;
    public double radius;
    public double mass;
    public boolean ballStopped;

    public static final double maxSpeed = 5.0;

    public Ball(Vector2 startPosition, Vector2 startVelocity) {
        state = new BallState(startPosition, startVelocity);
        mass = 1;
        radius = 0.25;
    }

    public void addForce(Vector2 force) {
        Vector2 velocityChange = force.scale(1 / mass);
        state.velocity.translate(velocityChange);
        ballStopped = false;
    }

    /**
     * Use state.getZCoordinate instead
     */
    @Deprecated
    public double getZCoordinate(Terrain terrain) {
        return terrain.getTerrainFunction().valueAt(state.position.x, state.position.y);
    }

    public Ball copy() {
        Ball newBall = new Ball(state.position.copy(), state.velocity.copy());
        newBall.mass = mass;
        newBall.radius = radius;
        return newBall;
    }
}
