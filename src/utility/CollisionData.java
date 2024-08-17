package utility;

import utility.math.Vector2;

public class CollisionData {
    public Vector2 collisionNormal;
    public Vector2 collisionPosition;
    public Vector2 previousPosition;
    public double bounciness;
    public double ballRadius;

    @Override
    public String toString(){
        return "Normal: " + collisionNormal + ", position: " + collisionPosition + ", bounciness: " + bounciness;
    }
}