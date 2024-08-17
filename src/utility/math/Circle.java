package utility.math;

public class Circle extends Shape {
    public Circle(){
        
    }
    public Circle(Vector2 originPosition, double radius) {
        this.originPosition = originPosition;
        this.radius = radius;
    }

    public Vector2 originPosition;
    public double radius;

    @Override
    protected boolean isPositionInside(Vector2 objectPosition) {
        Vector2 deltaPos = objectPosition.copy().translate(originPosition.reversed());
        double distance = deltaPos.length();

        boolean isInside = distance < radius;
        if (isInside) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean isCircleInside(Vector2 objectPosition, double ballRadius) {
        Vector2 deltaPos = objectPosition.copy().translate(originPosition.reversed());
        double distance = deltaPos.length();

        boolean isInside = distance < radius + ballRadius;
        if (isInside) {
            return true;
        }
        return false;
    }
}