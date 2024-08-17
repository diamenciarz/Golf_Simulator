package datastorage.obstacles;

import java.util.ArrayList;

import utility.CollisionData;
import utility.UtilityClass;
import utility.math.InfLine2D;
import utility.math.Rectangle;
import utility.math.Vector2;

public class ObstacleBox extends Rectangle implements IObstacle {
    private static int staticID=0;
    private final int id;
    public ObstacleBox(Vector2 bottomLeftCorner, Vector2 topRightCorner){
        super(bottomLeftCorner, topRightCorner);
        bounciness = 0.75;
        staticID++;
        id = staticID;
    }

    @Override
    public int getId() {
        return id;
    }

    // This is basically friction for bounces
    public double bounciness; // The percentage of momentum that the ball keeps after bouncing.

    private double ballRadius; // This is a temporary global variable to not pass it as a parameter everywhere

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

        this.ballRadius = ballRadius;
        Vector2[] wall = getCollisionPointAndWall(previousPosition, currentPosition);

        if(wall == null){
            return null;
        }

        CollisionData collisionData = new CollisionData();

        Vector2 wallDirectionVector = wall[1].translated(wall[0].reversed());
        Vector2 normal = wallDirectionVector;
        collisionData.collisionNormal = normal.getPerpendicularVector();

        collisionData.bounciness = bounciness;
        collisionData.collisionPosition = wall[2];

        collisionData.ballRadius = ballRadius;
        collisionData.previousPosition = previousPosition;

        return collisionData;
    }

    /**
     *
     * @return Two corners of the wall and the collision point as a third value
     * or null if the object did not collide with any wall
     */
    private Vector2[] getCollisionPointAndWall(Vector2 firstPosition, Vector2 secondPosition) {
        Vector2[] collisionPoints = getAllCrossPoints(firstPosition, secondPosition);
        Vector2 closestPoint = UtilityClass.getClosestPoint(firstPosition, collisionPoints);

        if (closestPoint == null) {
            return null;
        }

        if (collidedFrom(closestPoint, getLeftWall())) {
            return constructCollisionPointsAndWall(closestPoint, getLeftWall());
        }

        if (collidedFrom(closestPoint, getRightWall())) {
            return constructCollisionPointsAndWall(closestPoint, getRightWall());
        }

        if (collidedFrom(closestPoint, getTopWall())) {
            return constructCollisionPointsAndWall(closestPoint, getTopWall());
        }

        if (collidedFrom(closestPoint, getBottomWall())) {
            return constructCollisionPointsAndWall(closestPoint, getBottomWall());
        }
        return null; // This should never happen
    }

    //region Helper methods
    private Vector2[] constructCollisionPointsAndWall(Vector2 point, Vector2[] wall){
        Vector2[] returnArray = new Vector2[3];

        returnArray[0] = wall[0];
        returnArray[1] = wall[1];
        returnArray[2] = point;

        return returnArray;
    }

    private boolean collidedFrom(Vector2 closestPoint, Vector2[] wall){
        return UtilityClass.isPointInEpisode(closestPoint, wall[0], wall[1]);
    }
    //endregion

    /**
     * Explanation: https://drive.google.com/file/d/1wa3YOD5C4TxELWLMZ5EReX7EMyXXX-RP/view?usp=sharing
     * @param firstPosition
     * @param secondPosition
     * @return
     */
    private Vector2[] getAllCrossPoints(Vector2 firstPosition, Vector2 secondPosition){
        ArrayList<Vector2> allCrossPoints = new ArrayList<>();
        InfLine2D pathLine = new InfLine2D(firstPosition, secondPosition);
        double offsetScale = 1 - Math.abs(Math.sin(pathLine.getSlopeAngle()));

        addCrossPointsWithWalls(allCrossPoints, firstPosition, secondPosition, offsetScale);
        //Internal lines have priority
        if (listHasNonNullValues(allCrossPoints)) {
            return allCrossPoints.toArray(new Vector2[0]);
        }
        //Last position check
        addCrossPointsAtPosition(allCrossPoints, secondPosition);
        return allCrossPoints.toArray(new Vector2[0]);
    }

    private boolean listHasNonNullValues(ArrayList<Vector2> allCrossPoints){
        for (Vector2 point : allCrossPoints) {
            if (point != null) {
                return true;
            }
        }
        return false;
    }

    //region Edge position collisions
    /**
     * Explanation: https://drive.google.com/file/d/1wPrPgXEMswSXUWUwZ-OXxxqcHV55Nvlh/view?usp=sharing
     * @param allCrossPoints
     * @param position
     */
    private void addCrossPointsAtPosition(ArrayList<Vector2> allCrossPoints,Vector2 position){
        ArrayList<Vector2> crossPointsFirstPosition = getCrossPointsAtPosition(position);

        for (Vector2 point : crossPointsFirstPosition) {
            if (point == null) {
                continue;
            }
            allCrossPoints.add(point);
        }
    }

    private ArrayList<Vector2> getCrossPointsAtPosition(Vector2 position){
        ArrayList<Vector2> allCrossPointsAtPosition = new ArrayList<>();
        allCrossPointsAtPosition.addAll(getCrossPointsInEpisode(getRightWall(), position));
        allCrossPointsAtPosition.addAll(getCrossPointsInEpisode(getLeftWall(), position));
        allCrossPointsAtPosition.addAll(getCrossPointsInEpisode(getTopWall(), position));
        allCrossPointsAtPosition.addAll(getCrossPointsInEpisode(getBottomWall(), position));

        return allCrossPointsAtPosition;
    }

    private ArrayList<Vector2> getCrossPointsInEpisode(Vector2[] episode, Vector2 position){
        InfLine2D wallLine = new InfLine2D(episode[0], episode[1]);
        ArrayList<Vector2> crossPointsWithWall = wallLine.getCrossPointsWithCircle(position, ballRadius);

        if (crossPointsWithWall.size() == 0) {
            return new ArrayList<Vector2>();
        }

        removePointsOutsideOfEpisode(crossPointsWithWall, episode);

        return crossPointsWithWall;
    }

    private void removePointsOutsideOfEpisode(ArrayList<Vector2> points, Vector2[] episode){
        for (int i = 0; i < points.size(); i++) {
            Vector2 point = points.get(i);
            if (!UtilityClass.isPointInEpisode(point, episode[0], episode[1])) {
                points.set(i, null);
            }
        }
    }
    //endregion

    //region WallCollisions
    /**
     * Explanation: https://drive.google.com/file/d/1wOqDEP274ktAh7sB_yPRSPYpBHhZX1-g/view?usp=sharing
     * Naming convention visualization: https://drive.google.com/file/d/1wnTsUU4qBOOv4HA1YsOYm9POoOd10-N7/view?usp=sharing
     * @param allCrossPoints
     * @param firstPosition
     * @param secondPosition
     */
    private void addCrossPointsWithWalls(ArrayList<Vector2> allCrossPoints, Vector2 firstPosition, Vector2 secondPosition, double offsetScale){
        InfLine2D pathLine = new InfLine2D(firstPosition, secondPosition);
        //Now we need the two lines that describe the edges of the ball's path
        Vector2[] firstParallelEpisode = getPathTravelledEpisode(pathLine, firstPosition, secondPosition, true, offsetScale);
        Vector2[] secondParallelEpisode = getPathTravelledEpisode(pathLine, firstPosition, secondPosition, false, offsetScale);

        //Check for the two side lines
        Vector2[] crossPointsThroughFirstParallel = getCrossPointsWithWalls(firstParallelEpisode[0], firstParallelEpisode[1]);
        Vector2[] crossPointsThroughSecondParallel = getCrossPointsWithWalls(secondParallelEpisode[0], secondParallelEpisode[1]);
        addNonNullCrossPoints(allCrossPoints, crossPointsThroughFirstParallel);
        addNonNullCrossPoints(allCrossPoints, crossPointsThroughSecondParallel);
    }

    private void addNonNullCrossPoints(ArrayList<Vector2> allCrossPoints, Vector2[] crossPoints){
        for (Vector2 point : crossPoints) {
            if (point != null) {
                allCrossPoints.add(point);
            }
        }
    }

    private Vector2[] getPathTravelledEpisode(InfLine2D pathLine, Vector2 firstPosition, Vector2 secondPosition, boolean reverseTranslation, double offsetScale){
        InfLine2D perpendicularToPathAtFirstPosition = pathLine.getPerpendicularLineAtPoint(firstPosition);
        Vector2 translationToParallelLine = perpendicularToPathAtFirstPosition.getDirectionVector(ballRadius);

        //Set the translation vector's length and direction
        scaleTranslationVector(translationToParallelLine, offsetScale, reverseTranslation);

        //Offset the lines by a translation vector.
        Vector2 firstEpisodePosition = firstPosition.translated(translationToParallelLine);
        InfLine2D parallelLine = pathLine.getLineTranslatedByVector(translationToParallelLine);
        //Get the further cross point with circle
        Vector2 circleCrossPoint = getFurtherCrossPoint(pathLine, parallelLine, firstPosition, secondPosition);

        Vector2[] episode = new Vector2[2];
        episode[0] = firstEpisodePosition;
        // Second Episode Position
        episode[1] = circleCrossPoint;
        return episode;
    }

    private void scaleTranslationVector(Vector2 translation, double scale, boolean reverseTranslation){
        translation.scale(scale);
        if (reverseTranslation) {
            translation.reverse();
        }
    }

    private Vector2 getFurtherCrossPoint(InfLine2D pathLine, InfLine2D parallelLine, Vector2 firstPosition, Vector2 secondPosition){
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
     * @return a list of 4 positions that save the collision point of the ball with the wall.
     * If a point is null, then no collision with that wall occured
     */
    public Vector2[] getCrossPointsWithWalls(Vector2 firstPosition, Vector2 secondPosition){
        if (firstPosition == null || secondPosition == null) {
            return new Vector2[2];
        }

        Vector2[] crossPoints = new Vector2[4];

        Vector2[] leftWall = getLeftWall();
        crossPoints[0] = UtilityClass.findEpisodeIntersection(firstPosition, secondPosition, leftWall[0], leftWall[1]);
        Vector2[] rightWall = getRightWall();
        crossPoints[1] = UtilityClass.findEpisodeIntersection(firstPosition, secondPosition, rightWall[0], rightWall[1]);
        Vector2[] topWall = getTopWall();
        crossPoints[2] = UtilityClass.findEpisodeIntersection(firstPosition, secondPosition, topWall[0], topWall[1]);
        Vector2[] bottomWall = getBottomWall();
        crossPoints[3] = UtilityClass.findEpisodeIntersection(firstPosition, secondPosition, bottomWall[0], bottomWall[1]);

        return crossPoints;
    }
    //endregion

    private Vector2[] getBottomWall() {
        Vector2[] wall = new Vector2[2];
        wall[0] = bottomLeftCorner;
        wall[1] = new Vector2(topRightCorner.x, bottomLeftCorner.y);
        return wall;
    }

    private Vector2[] getTopWall() {
        Vector2[] wall = new Vector2[2];
        wall[0] = topRightCorner;
        wall[1] = new Vector2(bottomLeftCorner.x, topRightCorner.y);
        return wall;
    }

    private Vector2[] getRightWall() {
        Vector2[] wall = new Vector2[2];
        wall[0] = topRightCorner;
        wall[1] = new Vector2(topRightCorner.x, bottomLeftCorner.y);
        return wall;
    }

    private Vector2[] getLeftWall() {
        Vector2[] wall = new Vector2[2];
        wall[0] = bottomLeftCorner;
        wall[1] = new Vector2(bottomLeftCorner.x, topRightCorner.y);
        return wall;
    }

    public Vector2 getCollisionNormal(Vector2 position, Vector2 velocity) {
        boolean collidedFromLeft = position.x < bottomLeftCorner.x;
        if (collidedFromLeft) {
            return Vector2.leftVector();
        }
        boolean collidedFromRight = position.x > topRightCorner.x;
        if (collidedFromRight) {
            return Vector2.rightVector();
        }
        boolean collidedFromTop = position.y > topRightCorner.y;
        if (collidedFromTop) {
            return Vector2.upVector();
        }
        // collidedFromBottom = position.y < downLeftCorner.y;
        return Vector2.downVector();
    }

    @Override
    public void print() {
        System.out.println("Box: ");
        System.out.print("Down left corner: ");
        System.out.println(bottomLeftCorner);
        System.out.print("Top right corner: ");
        System.out.println(topRightCorner);
        System.out.print("Bounciness: ");
        System.out.println(bounciness);
    }
}
