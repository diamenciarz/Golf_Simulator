package utility.math;

import utility.UtilityClass;

public class Rectangle extends Shape {
    public Rectangle(){
    }

    public Rectangle(Vector2 bottomLeftCorner, Vector2 topRightCorner){
        this.bottomLeftCorner = bottomLeftCorner;
        this.topRightCorner = topRightCorner;
        UtilityClass.ensureCornersAreRight(this.bottomLeftCorner, this.topRightCorner);
    }

    public Vector2 bottomLeftCorner;
    public Vector2 topRightCorner;

    @Override
    public boolean isPositionInside(Vector2 objectPosition) {
        boolean isXInside = objectPosition.x > bottomLeftCorner.x && objectPosition.x < topRightCorner.x;
        boolean isYInside = objectPosition.y < bottomLeftCorner.y && objectPosition.y > topRightCorner.y;
        if (isXInside && isYInside) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean isCircleInside(Vector2 objectPosition, double ballRadius) {
        boolean isXInside = objectPosition.x + ballRadius > bottomLeftCorner.x && objectPosition.x - ballRadius < topRightCorner.x;
        boolean isYInside = objectPosition.y - ballRadius < bottomLeftCorner.y && objectPosition.y + ballRadius > topRightCorner.y;
        if (isXInside && isYInside) {
            return true;
        }

        boolean touchesBottomLeftCorner = objectPosition.distanceTo(bottomLeftCorner) < ballRadius;
        boolean touchesBottomRightCorner = objectPosition.distanceTo(new Vector2(topRightCorner.x, bottomLeftCorner.y)) < ballRadius;
        boolean touchesTopRightCorner = objectPosition.distanceTo(topRightCorner) < ballRadius;
        boolean touchesTopLeftCorner = objectPosition.distanceTo(new Vector2(bottomLeftCorner.x, topRightCorner.y)) < ballRadius;
        if (touchesBottomLeftCorner || touchesBottomRightCorner || touchesTopRightCorner || touchesTopLeftCorner) {
            return true;
        }
        return false;
    }
}