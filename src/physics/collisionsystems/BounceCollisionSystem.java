package physics.collisionsystems;

import java.util.ArrayList;

import datastorage.*;
import datastorage.obstacles.IObstacle;
import utility.CollisionData;
import utility.UtilityClass;
import utility.math.Vector2;

public class BounceCollisionSystem implements ICollisionSystem {
    public ArrayList<IObstacle> obstacles;

    public BallState modifyStateDueToCollisions(BallState state, BallState previousState, double ballRadius, Terrain terrain) {
        Vector2 previousPosition = previousState.position;
        obstacles = terrain.obstacles;
        double searchRadius = state.position.distanceTo(previousPosition) + ballRadius;
        ArrayList<IObstacle> collidesWith = getTouchedObstacles(previousPosition, searchRadius);
        CollisionData collisionData = getClosestCollisionData(collidesWith, state.position, previousPosition,
                ballRadius);
        if (collisionData != null) {
            bounceBall(state, collisionData);
        }

        state.position = handleBallOutOfBounds(state, terrain);
        return state;
    }

    /**
     *
     * @param position
     * @return the obstacle that the ball collided with or null if it didn't
     */
    private ArrayList<IObstacle> getTouchedObstacles(Vector2 position, double searchRadius) {
        ArrayList<IObstacle> touchedObstacles = new ArrayList<>();
        for (IObstacle obstacle : obstacles) {
            if (obstacle.isBallColliding(position, searchRadius)) {
                touchedObstacles.add(obstacle);
            }
        }
        return touchedObstacles;
    }

    private CollisionData getClosestCollisionData(ArrayList<IObstacle> collidesWith, Vector2 currentPosition,
                                                  Vector2 previousPosition, double ballRadius) {
        CollisionData closestCollisionData = null;
        for (IObstacle iObstacle : collidesWith) {
            if (iObstacle == null) {
                continue;
            }
            CollisionData collisionData = iObstacle.getCollisionData(currentPosition, previousPosition, ballRadius);
            if (collisionData == null) {
                continue;
            }
            if (closestCollisionData == null) {
                closestCollisionData = collisionData;
                continue;
            }
            boolean foundACloserCollision = previousPosition
                    .distanceTo(closestCollisionData.collisionPosition) > previousPosition
                    .distanceTo(collisionData.collisionPosition);
            if (foundACloserCollision) {
                closestCollisionData = collisionData;
            }
        }
        return closestCollisionData;
    }

    private void bounceBall(BallState state, CollisionData collisionData) {
        calculateVelocityAfterCollision(state, collisionData);
        calculatePositionAfterCollision(state, collisionData);
    }

    private void calculateVelocityAfterCollision(BallState state, CollisionData collisionData) {
        state.velocity.reflect(collisionData.collisionNormal);
        // For eg. if bounciness equals 0.8, the returned velocity vector will be 20%
        // shorter
        state.velocity.scale(collisionData.bounciness);
    }

    private void calculatePositionAfterCollision(BallState state, CollisionData collisionData) {
        // Calculation variables
        Vector2 collisionPositionMinusRadius = countCollisionPositionMinusRadius(state, collisionData);
        double moveDistanceAfterCollision = state.position.distanceTo(collisionPositionMinusRadius);
        Vector2 moveVectorAfterCollision = state.velocity.normalized().scale(moveDistanceAfterCollision);

        // Calculating the new position
        Vector2 positionAfterCollision = collisionPositionMinusRadius.translated(moveVectorAfterCollision);
        state.position = positionAfterCollision;
    }

    private Vector2 countCollisionPositionMinusRadius(BallState state, CollisionData collisionData) {
        Vector2 collisionPos = collisionData.collisionPosition;
        Vector2 translationByRadius = collisionData.collisionNormal.normalized().scale(collisionData.ballRadius);

        Vector2 fromPreviousToCurrentPos = collisionData.previousPosition.deltaPositionTo(state.position);
        double dot = Vector2.dotProduct(fromPreviousToCurrentPos, collisionData.collisionNormal);
        if (dot < 0) {
            return collisionPos.translated(translationByRadius);
        }
        return collisionPos.translated(translationByRadius.reverse());
    }

    private Vector2 handleBallOutOfBounds(BallState state, Terrain terrain) {
        Vector2 newPosition = state.position.copy();
        boolean reverseX = false;
        if (newPosition.x > terrain.bottomRightCorner.x) {
            reverseX = true;
            Vector2 intersectionPoint = UtilityClass.findLineIntersection(state.position, newPosition,
                    new Vector2(terrain.bottomRightCorner.x, 0),
                    new Vector2(terrain.bottomRightCorner.x, 1));
            if (intersectionPoint != null) {
                newPosition = intersectionPoint;
            } else {
                newPosition = state.position;
            }
        } else if (newPosition.x < terrain.topLeftCorner.x) {
            reverseX = true;
            Vector2 intersectionPoint = UtilityClass.findLineIntersection(state.position, newPosition,
                    new Vector2(terrain.topLeftCorner.x, 0),
                    new Vector2(terrain.topLeftCorner.x, 1));
            if (intersectionPoint != null) {
                newPosition = intersectionPoint;
            } else {
                newPosition = state.position;
            }
        }
        // On y-axis
        boolean reverseY = false;
        if (newPosition.y > terrain.bottomRightCorner.y) {
            reverseY = true;
            Vector2 intersectionPoint = UtilityClass.findLineIntersection(state.position, newPosition,
                    new Vector2(0, terrain.bottomRightCorner.y),
                    new Vector2(0, terrain.bottomRightCorner.y));
            if (intersectionPoint != null) {
                newPosition = intersectionPoint;
            } else {
                newPosition = state.position;
            }
        } else if (newPosition.y < terrain.topLeftCorner.y) {
            reverseY = true;
            Vector2 intersectionPoint = UtilityClass.findLineIntersection(state.position, newPosition,
                    new Vector2(0, terrain.topLeftCorner.y),
                    new Vector2(0, terrain.topLeftCorner.y));
            if (intersectionPoint != null) {
                newPosition = intersectionPoint;
            } else {
                newPosition = state.position;
            }
        }
        // Reverse the velocity if needed
        if (reverseX) {
            state.velocity.x = -state.velocity.x;
        }
        if (reverseY) {
            state.velocity.y = -state.velocity.y;
        }
        return newPosition;
    }
    
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "bounce";
    }
}
