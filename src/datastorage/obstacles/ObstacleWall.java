package datastorage.obstacles;

import java.util.ArrayList;

import utility.CollisionData;
import utility.UtilityClass;
import utility.math.Episode;
import utility.math.InfLine2D;
import utility.math.Vector2;

public class ObstacleWall extends Episode implements IObstacle {
    private static int staticID = 0;
    private final int id;

    public ObstacleWall(Vector2 firstPosition, Vector2 secondPosition) {
        super(firstPosition, secondPosition);
        createCorners();
        staticID++;
        id = staticID;
    }

    public ObstacleWall(Vector2 firstPosition, Vector2 secondPosition, double bounciness) {
        super(firstPosition, secondPosition);
        this.bounciness = bounciness;
        createCorners();
        staticID++;
        id = staticID;
    }

    @Override
    public int getId() {
        return id;
    }

    private void createCorners() {
        firstCorner = new ObstacleTree(firstPosition, wallThickness, bounciness);
        secondCorner = new ObstacleTree(secondPosition, wallThickness, bounciness);
    }

    private double bounciness = 0.9; // The percentage of momentum that the ball loses after bouncing.
    // This is basically friction for bounces
    private final double wallThickness = 0.05d;
    private double ballRadius;

    private ObstacleTree firstCorner;
    private ObstacleTree secondCorner;

    // region Accessor methods
    @Override
    public boolean isBallColliding(Vector2 ballPos, double radius) {
        return isCircleInside(ballPos, radius);
    }

    @Override
    public boolean isPositionColliding(Vector2 position) {
        Vector2 crossPoint = lineRepresentation.getClosestPointOnLineToPosition(position);
        if (!UtilityClass.isPointInEpisode(crossPoint, firstPosition, secondPosition)) {
            return false;
        }
        double distanceToPosition = lineRepresentation.getDistanceToPoint(position);
        return distanceToPosition <= wallThickness;
    }

    @Override
    public double getBounciness() {
        return bounciness;
    }

    public double getWallThickness() {
        return wallThickness;
    }

    @Override
    public CollisionData getCollisionData(Vector2 currentPosition, Vector2 previousPosition, double ballRadius) {
        this.ballRadius = ballRadius;
        CollisionData cornerCollisionData = getCornerCollisionData(currentPosition, previousPosition);
        if (cornerCollisionData != null) {
            return cornerCollisionData;
        }

        Vector2 collisionPoint = getCollisionPoint(previousPosition, currentPosition);

        if (collisionPoint == null) {
            return null;
        }

        CollisionData collisionData = new CollisionData();

        collisionData.collisionPosition = collisionPoint;
        collisionData.collisionNormal = lineRepresentation.getPerpendicularLineAtPoint(collisionPoint)
                .getDirectionVector();
        collisionData.bounciness = bounciness;
        collisionData.ballRadius = ballRadius;
        collisionData.previousPosition = previousPosition;

        return collisionData;
    }

    /**
     *
     * @return Two corners of the wall and the collision point as a third value
     *         or null if the object did not collide with any wall
     */
    private Vector2 getCollisionPoint(Vector2 firstPosition, Vector2 secondPosition) {
        Vector2[] collisionPoints = getAllCrossPoints(firstPosition, secondPosition);
        Vector2 closestPoint = UtilityClass.getClosestPoint(firstPosition, collisionPoints);

        return closestPoint;
    }

    // region Corner collisions
    private CollisionData getCornerCollisionData(Vector2 currentPosition, Vector2 previousPosition) {
        CollisionData firstCollisionData = firstCorner.getCollisionData(currentPosition, previousPosition, ballRadius);
        CollisionData secondCollisionData = secondCorner.getCollisionData(currentPosition, previousPosition,
                ballRadius);

        if (firstCollisionData == null) {
            if (secondCollisionData != null) {
                return secondCollisionData;
            }
            return null;
        }
        if (secondCollisionData == null) {
            if (firstCollisionData != null) {
                return firstCollisionData;
            }
        }
        double firstDistance = firstCollisionData.collisionPosition.distanceTo(previousPosition);
        double secondDistance = secondCollisionData.collisionPosition.distanceTo(previousPosition);
        if (firstDistance < secondDistance) {
            return firstCollisionData;
        }
        return secondCollisionData;
    }
    // endregion

    /**
     * Explanation:
     * https://drive.google.com/file/d/1wa3YOD5C4TxELWLMZ5EReX7EMyXXX-RP/view?usp=sharing
     * 
     * @param firstPosition
     * @param secondPosition
     * @return
     */
    private Vector2[] getAllCrossPoints(Vector2 firstPosition, Vector2 secondPosition) {
        ArrayList<Vector2> allCrossPoints = new ArrayList<>();
        InfLine2D pathLine = new InfLine2D(firstPosition, secondPosition);
        double offsetScale = 1 - Math.abs(Math.sin(pathLine.getSlopeAngle()));

        addCrossPointsWithWalls(allCrossPoints, firstPosition, secondPosition, offsetScale);
        // Internal lines have priority
        if (listHasNonNullValues(allCrossPoints)) {
            return allCrossPoints.toArray(new Vector2[0]);
        }
        // Last position check
        return getCrossPointsInEpisode(secondPosition).toArray(new Vector2[0]);
    }

    // region WallCollisions
    /**
     * Explanation:
     * https://drive.google.com/file/d/1wOqDEP274ktAh7sB_yPRSPYpBHhZX1-g/view?usp=sharing
     * Naming convention visualization:
     * https://drive.google.com/file/d/1wnTsUU4qBOOv4HA1YsOYm9POoOd10-N7/view?usp=sharing
     * 
     * @param allCrossPoints
     * @param firstPosition
     * @param secondPosition
     */
    private void addCrossPointsWithWalls(ArrayList<Vector2> allCrossPoints, Vector2 firstPosition,
            Vector2 secondPosition, double offsetScale) {
        InfLine2D pathLine = new InfLine2D(firstPosition, secondPosition);
        // Now we need the two lines that describe the edges of the ball's path
        Episode firstParallelEpisode = getPathTravelledEpisode(pathLine, firstPosition, secondPosition, true,
                offsetScale);
        Episode secondParallelEpisode = getPathTravelledEpisode(pathLine, firstPosition, secondPosition, false,
                offsetScale);

        // Check for the two side lines
        addNonNullCrossPoint(allCrossPoints, getCrossPointWithWall(firstParallelEpisode));
        addNonNullCrossPoint(allCrossPoints, getCrossPointWithWall(secondParallelEpisode));
    }

    private void addNonNullCrossPoint(ArrayList<Vector2> allCrossPoints, Vector2 crossPoint) {
        if (crossPoint != null) {
            allCrossPoints.add(crossPoint);
        }
    }

    private Episode getPathTravelledEpisode(InfLine2D pathLine, Vector2 firstPosition, Vector2 secondPosition,
            boolean reverseTranslation, double offsetScale) {
        InfLine2D perpendicularToPathAtFirstPosition = pathLine.getPerpendicularLineAtPoint(firstPosition);
        Vector2 translationToParallelLine = perpendicularToPathAtFirstPosition.getDirectionVector(ballRadius);

        // Set the translation vector's length and direction
        scaleTranslationVector(translationToParallelLine, offsetScale, reverseTranslation);

        // Offset the lines by a translation vector.
        Vector2 firstEpisodePosition = firstPosition.translated(translationToParallelLine);
        InfLine2D parallelLine = pathLine.getLineTranslatedByVector(translationToParallelLine);
        // Get the further cross point with circle
        Vector2 circleCrossPoint = getFurtherCrossPoint(pathLine, parallelLine, firstPosition, secondPosition);

        return new Episode(firstEpisodePosition, circleCrossPoint);
    }

    private void scaleTranslationVector(Vector2 translation, double scale, boolean reverseTranslation) {
        translation.scale(scale);
        if (reverseTranslation) {
            translation.reverse();
        }
    }

    private Vector2 getFurtherCrossPoint(InfLine2D pathLine, InfLine2D parallelLine, Vector2 firstPosition,
            Vector2 secondPosition) {
        ArrayList<Vector2> crossPoints = parallelLine.getCrossPointsWithCircle(secondPosition, ballRadius);
        if (crossPoints.size() == 0) {
            return pathLine.getCrossPointWithLine(parallelLine);
        }
        double distanceToFirstPoint = firstPosition.distanceTo(crossPoints.get(0));
        double distanceToSecondPoint = firstPosition.distanceTo(crossPoints.get(1));
        if (distanceToFirstPoint > distanceToSecondPoint) {
            return crossPoints.get(0);
        }
        return crossPoints.get(1);
    }

    /**
     * @return a list of 4 positions that save the collision point of the ball with
     *         the wall.
     *         If a point is null, then no collision with that wall occured
     */
    public Vector2 getCrossPointWithWall(Episode episode) {
        if (episode == null) {
            return null;
        }
        return UtilityClass.findEpisodeIntersection(episode.firstPosition, episode.secondPosition,
                firstPosition, secondPosition);
    }
    // endregion

    private boolean listHasNonNullValues(ArrayList<Vector2> allCrossPoints) {
        for (Vector2 point : allCrossPoints) {
            if (point != null) {
                return true;
            }
        }
        return false;
    }

    // region Edge position collisions
    private ArrayList<Vector2> getCrossPointsInEpisode(Vector2 circleOrigin) {
        ArrayList<Vector2> crossPointsWithWall = lineRepresentation.getCrossPointsWithCircle(circleOrigin, ballRadius);

        if (crossPointsWithWall.size() == 0) {
            return new ArrayList<Vector2>();
        }

        removePointsOutsideOfEpisode(crossPointsWithWall);
        return crossPointsWithWall;
    }

    private void removePointsOutsideOfEpisode(ArrayList<Vector2> points) {
        for (int i = 0; i < points.size(); i++) {
            Vector2 point = points.get(i);
            if (!UtilityClass.isPointInEpisode(point, firstPosition, secondPosition)) {
                points.set(i, null);
            }
        }
    }
    // endregion

    public ObstacleWall copy() {
        return new ObstacleWall(firstPosition.copy(), secondPosition.copy(), bounciness);
    }

    public void setBounciness(double bounciness) {
        this.bounciness = bounciness;
        createCorners();
    }

    @Override
    public void print() {
        System.out.println("Wall: ");
        System.out.println("First position: " + firstPosition);
        System.out.println("Second position: " + secondPosition);
        System.out.println("Bounciness: " + bounciness);
    }
}
