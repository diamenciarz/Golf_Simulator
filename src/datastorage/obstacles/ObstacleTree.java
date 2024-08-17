package datastorage.obstacles;

import java.util.ArrayList;

import utility.CollisionData;
import utility.math.Circle;
import utility.math.InfLine2D;
import utility.math.Vector2;

public class ObstacleTree extends Circle implements IObstacle {
    private static int staticID=0;
    private final int id;
    public ObstacleTree(){
        super();
        staticID++;
        id = staticID;
    }

    public ObstacleTree(Vector2 originPosition, double radius, double bounciness) {
        super(originPosition, radius);
        this.bounciness = bounciness;
        staticID++;
        id = staticID;
    }

    @Override
    public int getId() {
        return id;
    }

    public double bounciness; // The percentage of momentum that the ball loses after bouncing.
    // This is basically friction for bounces

    @Override
    public boolean isBallColliding(Vector2 ballPos, double radius) {
        return isCircleInside(ballPos, radius);
    }

    @Override
    public boolean isPositionColliding(Vector2 position) {
        return isPositionInside(position);
    }

    @Override
    public double getBounciness() {
        return bounciness;
    }

    @Override
    public CollisionData getCollisionData(Vector2 currentPosition, Vector2 previousPosition, double ballRadius) {
        Vector2[] collisionPoints = getCollisionPoints(currentPosition, previousPosition, ballRadius);

        if (collisionPoints == null) {
            return null;
        }

        Vector2 closestCollisionPoint = getClosestCollisionPoint(collisionPoints, previousPosition);

        CollisionData collisionData = new CollisionData();
        collisionData.collisionPosition = closestCollisionPoint;

        collisionData.bounciness = bounciness;
        collisionData.collisionNormal = getNormal(closestCollisionPoint);

        collisionData.ballRadius = 0;
        collisionData.previousPosition = previousPosition;

        return collisionData;
    }

    /**
     * The whole outline of the geometry is here
     * https://drive.google.com/file/d/1rt7-Zydk_wbFvLNzdlaMwInoSB4xYM0x/view?usp=sharing
     * 
     * @return two collision points with this obstacle (the same one twice if it is
     *         a tangent)
     *         or null if there is no collision
     */
    private Vector2[] getCollisionPoints(Vector2 currentPosition, Vector2 previousPosition, double ballRadius) {
        InfLine2D movementLine = new InfLine2D(currentPosition, previousPosition);
        ArrayList<Vector2> collisionsWithLine = movementLine.getCrossPointsWithCircle(originPosition,
                radius + ballRadius);

        if (collisionsWithLine.size() == 0) {
            return null;
        }

        // Vector2 deltaPositionToCollisionPoint =
        // previousPosition.translated(collisionsWithLine.get(0).reversed()).normalize().scale(ballRadius).reverse();
        Vector2 deltaPositionToCollisionPoint = Vector2.zeroVector();
        Vector2[] collisionPoints = new Vector2[2];
        collisionPoints[0] = collisionsWithLine.get(0).translated(deltaPositionToCollisionPoint);
        collisionPoints[1] = collisionsWithLine.get(1).translated(deltaPositionToCollisionPoint);

        return collisionPoints;
    }

    private Vector2 getClosestCollisionPoint(Vector2[] collisionPoints, Vector2 previousPosition) {
        if (collisionPoints[0].distanceTo(previousPosition) < collisionPoints[1].distanceTo(previousPosition)) {
            return collisionPoints[0];
        }
        return collisionPoints[1];
    }

    private Vector2 getNormal(Vector2 closestCollisionPoint) {
        return closestCollisionPoint.translated(originPosition.reversed());
    }

    public ObstacleTree copy() {
        ObstacleTree newTree = new ObstacleTree(originPosition, radius, bounciness);
        return newTree;
    }

    @Override
    public void print() {
        System.out.println("Tree: ");
        System.out.print("Position: ");
        System.out.println(originPosition);
        System.out.print("Radius: ");
        System.out.println(radius);
        System.out.print("Bounciness: ");
        System.out.println(bounciness);
    }
}
