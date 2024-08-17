package utility.math;

import java.util.ArrayList;

import utility.UtilityClass;

/**
 * An object representing an infinitely long line in 2D given as y = ax + b or described by two points
 */
public class InfLine2D {

    public InfLine2D(double slope, Vector2 passByPoint) {
        this.firstPosition = passByPoint;
        this.slope = slope;

        if (Double.isInfinite(slope)) {
            this.secondPosition = firstPosition.translated(Vector2.upVector());
            yZero = 0;
            return;
        }

        this.secondPosition = new Vector2(passByPoint.x + 1, passByPoint.y + slope);
        yZero = passByPoint.y - slope * passByPoint.x;
    }

    public InfLine2D(Vector2 firstPosition, Vector2 secondPosition) {
        this.firstPosition = firstPosition.copy();
        this.secondPosition = secondPosition.copy();
        slope = getSlope();

        if (Double.isInfinite(slope)) {
            yZero = 0;
        }

        yZero = firstPosition.y - slope * firstPosition.x;
    }

    Vector2 firstPosition;
    Vector2 secondPosition;

    double slope;
    double yZero;

    public Vector2 getFirstPosition() {
        return firstPosition.copy();
    }

    public Vector2 getSecondPosition() {
        return secondPosition.copy();
    }

    public InfLine2D copy() {
        return new InfLine2D(firstPosition, secondPosition);
    }

    /**
     * @return the tangent coefficient of this linear equation.
     *         Eg. if "y = ax + b", then it returns "a"
     */
    public double getSlope() {
        if (firstPosition.x == secondPosition.x) {
            return Double.POSITIVE_INFINITY;
        }
        return (firstPosition.y - secondPosition.y) / (firstPosition.x - secondPosition.x);
    }

    public double getSlopeAngle() {
        return Math.atan(slope);
    }

    /**
     * Calculates the distance to a given point
     * This is a solution taken from this website:
     * https://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
     * 
     * @param point
     * @return
     */
    public double getDistanceToPoint(Vector2 point) {
        double A = point.x - firstPosition.x;
        double B = point.y - firstPosition.y;
        double C = secondPosition.x - firstPosition.x;
        double D = secondPosition.y - firstPosition.y;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = -1;
        if (len_sq != 0) // in case of 0 length line
            param = dot / len_sq;

        double xx, yy;

        if (param < 0) {
            xx = firstPosition.x;
            yy = firstPosition.y;
        } else if (param > 1) {
            xx = secondPosition.x;
            yy = secondPosition.x;
        } else {
            xx = firstPosition.x + param * C;
            yy = firstPosition.y + param * D;
        }

        var dx = point.x - xx;
        var dy = point.y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Vector2 getShortestVectorToPoint(Vector2 point) {
        InfLine2D perpendicularLineThroughPoint = getPerpendicularLineAtPoint(point);
        Vector2 crossPoint = getCrossPointWithLine(perpendicularLineThroughPoint);

        return crossPoint.deltaPositionTo(point);
    }

    public Vector2 getClosestPointOnLineToPosition(Vector2 position){
        InfLine2D perpendicularLineThroughPoint = getPerpendicularLineAtPoint(position);
        Vector2 crossPoint = getCrossPointWithLine(perpendicularLineThroughPoint);
        return crossPoint;
    }

    /**
     * @return A new line that is perpendicular to this line and passes through the
     *         given point
     */
    public InfLine2D getPerpendicularLineAtPoint(Vector2 point) {
        double invertedCoefficient = -1 / slope;
        return new InfLine2D(invertedCoefficient, point.copy());
    }

    /**
     * @param point
     * @return A new line that is parallel to this line and passes through the given
     *         point
     */
    public InfLine2D getParallelLineAtPoint(Vector2 point) {
        return new InfLine2D(slope, point.copy());
    }

    public InfLine2D getLineTranslatedByVector(Vector2 translation) {
        Vector2 translatedPoint = firstPosition.translated(translation);
        return getParallelLineAtPoint(translatedPoint);
    }

    public Vector2 getPointAtX(double x) {
        if (slope == Double.POSITIVE_INFINITY) {
            if (x == firstPosition.x) {
                return new Vector2(x, 0);
            }
            return null;
        }

        Vector2 point = new Vector2(x, slope * x + yZero);
        return point;
    }

    public Vector2 getPointAtY(double y) {
        if (slope == 0) {
            if (y == firstPosition.y) {
                return new Vector2(0, y);
            }
            return null;
        }

        Vector2 point = new Vector2((firstPosition.y - yZero) / slope, y);
        return point;
    }

    /**
     * @return an unit vector pointing in the direction of the line, such that the x
     *         coordinate is positive.
     *         If the line is vertical, the returned vector will be (0,1)
     */
    public Vector2 getDirectionVector() {
        if (Double.isInfinite(slope)) {
            return Vector2.upVector();
        }

        Vector2 zeroPos = getPointAtX(0);
        Vector2 onePos = getPointAtX(1);
        return zeroPos.deltaPositionTo(onePos).normalize();
    }

    /**
     * @param length
     * @return an unit vector pointing in the direction of the line, such that the x
     *         coordinate is positive.
     *         If the line is vertical, the returned vector will be (0, length)
     */
    public Vector2 getDirectionVector(double length) {
        return getDirectionVector().scale(length);
    }

    /**
     * @return the position at which this line crosses another one. Returns null if
     *         they are parallel
     */
    public Vector2 getCrossPointWithLine(InfLine2D line) {
        return UtilityClass.findLineIntersection(firstPosition, secondPosition, line.firstPosition,
                line.secondPosition);
    }

    /**
     * Finds the cross points between this line and a defined circle
     * 
     * @param originPosition
     * @param radius
     * @return Two positions of cross points or an empty list, if there were no
     *         collisions
     */
    public ArrayList<Vector2> getCrossPointsWithCircle(Vector2 originPosition, double radius) {
        if (Double.isInfinite(slope)) {
            return countVerticalCrossPoints(originPosition, radius);
        }

        double yValue = getPointAtX(0).y;

        double a = 1 + slope * slope;
        double b = 2 * (slope * (yValue - originPosition.y) - originPosition.x);
        double c = originPosition.x * originPosition.x + (yValue - originPosition.y) * (yValue - originPosition.y)
                - radius * radius;

        double discriminant = b * b - 4 * a * c;

        ArrayList<Vector2> crossPoints = new ArrayList<>(2);

        if (discriminant == 0) {
            double x = ((-b + Math.sqrt(discriminant)) / (2 * a));
            Vector2 crossPoint = getPointAtX(x);
            crossPoints.add(crossPoint);
            crossPoints.add(crossPoint);
            return crossPoints;
        }
        if (discriminant > 0) {
            double x1 = ((-b + Math.sqrt(discriminant)) / (2 * a));
            double x2 = ((-b - Math.sqrt(discriminant)) / (2 * a));
            Vector2 crossPoint1 = getPointAtX(x1);
            Vector2 crossPoint2 = getPointAtX(x2);
            crossPoints.add(crossPoint1);
            crossPoints.add(crossPoint2);
            return crossPoints;
        }
        return new ArrayList<Vector2>();
    }

    private ArrayList<Vector2> countVerticalCrossPoints(Vector2 originPosition, double radius) {
        boolean touchesCircle = radius * radius >= (firstPosition.x - originPosition.x)
                * (firstPosition.x - originPosition.x);
        if (touchesCircle) {
            ArrayList<Vector2> crossPoints = new ArrayList<>(2);

            double root = Math.sqrt(
                    (radius * radius) - (firstPosition.x - originPosition.x) * (firstPosition.x - originPosition.x));

            crossPoints.add(new Vector2(firstPosition.x, root + originPosition.y));
            crossPoints.add(new Vector2(firstPosition.x, -root + originPosition.y));
            return crossPoints;
        }
        return new ArrayList<Vector2>();
    }

    @Override
    public String toString() {
        return slope + "*x + " + yZero;
    }
}
