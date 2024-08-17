package utility.math;

public abstract class Shape {

    protected abstract boolean isPositionInside(Vector2 objectPosition);

    protected abstract boolean isCircleInside(Vector2 objectPosition, double ballRadius);
}
