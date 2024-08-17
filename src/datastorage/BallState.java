package datastorage;

import utility.math.Vector2;

public class BallState {
    public Vector2 position;
    public Vector2 velocity;

    public BallState(Vector2 position, Vector2 velocity) {
        this.position = position;
        this.velocity = velocity;
    }

    public BallState copy() {
        BallState b = new BallState(position.copy(), velocity.copy());
        return b;
    }
    
    public double getZCoordinate(Terrain terrain) {
        return terrain.getTerrainFunction().valueAt(position.x, position.y);
    }
}
