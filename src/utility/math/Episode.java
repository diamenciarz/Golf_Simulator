package utility.math;

import java.util.ArrayList;

import utility.UtilityClass;

public class Episode extends Shape {

    public Episode(Vector2 firstPosition, Vector2 secondPosition) {
        this.firstPosition = firstPosition;
        this.secondPosition = secondPosition;
        lineRepresentation = new InfLine2D(firstPosition, secondPosition);
    }

    public Vector2 firstPosition;
    public Vector2 secondPosition;
    protected InfLine2D lineRepresentation;

    @Override
    public boolean isPositionInside(Vector2 objectPosition) {
        return UtilityClass.isPointInEpisode(objectPosition, firstPosition, secondPosition);
    }

    @Override
    public boolean isCircleInside(Vector2 objectPosition, double ballRadius) {
        ArrayList<Vector2> collisionPoints = lineRepresentation.getCrossPointsWithCircle(objectPosition, ballRadius);
        removePointsOutsideEpisode(collisionPoints);
        return collisionPoints.size() > 0;
    }

    private void removePointsOutsideEpisode(ArrayList<Vector2> collisionPoints) {
        for (int i = collisionPoints.size() - 1; i >= 0; i--) {
            boolean isPointInEpisode = UtilityClass.isPointInEpisode(collisionPoints.get(i), firstPosition,
                    secondPosition);
            if (!isPointInEpisode) {
                collisionPoints.remove(i);
            }
        }
    }

    public InfLine2D getInfiniteLineRepresentation() {
        return lineRepresentation;
    }
}
