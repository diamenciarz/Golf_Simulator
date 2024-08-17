package utility.math;

public class Vector2 {
    public double x;
    public double y;

    public Vector2() {
        x = 0;
        y = 0;
    }

    public Vector2(double newX, double newY) {
        x = newX;
        y = newY;
    }

    // readonly vectors
    public final static Vector2 zeroVector(){ return new Vector2(0, 0);}
    public final static Vector2 rightVector(){ return new Vector2(1, 0);}
    public final static Vector2 leftVector(){ return new Vector2(-1, 0);}
    public final static Vector2 upVector(){ return new Vector2(0, 1);}
    public final static Vector2 downVector(){ return new Vector2(0, -1);}
    public final static Vector2 unitVector(){ return new Vector2(1, 1);}

    // Helper methods
    public double length() {
        return Math.sqrt((x * x) + (y * y));
    }

    /**
     * @return this vector after it is translated
     */
    public Vector2 translate(Vector2 vector) {
        x += vector.x;
        y += vector.y;
        return this;
    }

    /**
     * @return this vector after it is translated
     */
    public Vector2 translate(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
        return this;
    }

    /**
     * @return a tanslated copy of this vector
     */
    public Vector2 translated(Vector2 vector) {
        return new Vector2(x + vector.x, y + vector.y);
    }

    /**
     * @return a tanslated copy of this vector
     */
    public Vector2 translated(double deltaX, double deltaY) {
        return new Vector2(x + deltaX, y + deltaY);
    }

    /**
     * reverses this vector and returns the result
     */
    public Vector2 reverse() {
        x *= -1;
        y *= -1;
        return this;
    }

    /**
     * reverses a copy of this vector and returns the result without modifying the original vector
     */
    public Vector2 reversed() {
        return new Vector2(-x, -y);
    }

    /**
     * scales this vector and returns the result
     */
    public Vector2 scale(double scale) {
        x*= scale;
        y*= scale;
        return this;
    }

    /**
     * scales a copy of this vector and returns the result without modifying the original vector
     */
    public Vector2 scaled(double scale) {
        return new Vector2(x * scale, y * scale);
    }

    /**
     * normalizes this vector and returns the result
     */
    public Vector2 normalize() {
        Vector2 normalizedVector = copy().scale(1 / length());
        x = normalizedVector.x;
        y = normalizedVector.y;

        return this;
    }

    /**
     * normalizes a copy of this vector and returns the result without modifying the original vector
     */
    public Vector2 normalized() {
        return copy().scale(1 / length());
    }

    /**
     * Adds this value to the vector's length. If the value is negative and higher than current length - the vector will go in the opposite direction
     * @param deltaLength
     * @return  this vector after its length was modified
     */
    public Vector2 modifyLength(double deltaLength){
        double currentLength = length();
        scale((currentLength + deltaLength) / currentLength);

        return this;
    }

    /**
     * Adds this value to the vector's length. If the value is negative and higher than current length - the vector will go in the opposite direction
     * @param deltaLength
     * @return  a copy of this vector with modified length
     */
    public Vector2 modifiedLength(double deltaLength){
        Vector2 newVector = copy();
        double currentLength = length();
        newVector.scale((currentLength + deltaLength) / currentLength);

        return newVector;
    }

    /**
     *
     * @param toVector
     * @return
     */
    public double distanceTo(Vector2 toVector){
        return Math.sqrt((x - toVector.x) * (x - toVector.x) + (y - toVector.y) * (y - toVector.y));
    }

    /**
     * @param toPosition
     * @return a delta position vector going from current position to the position given in the parameter. The arrow points to the target
     */
    public Vector2 deltaPositionTo(Vector2 toPosition){
        return new Vector2(toPosition.x - x, toPosition.y - y);
    }

    /**
     * Picture included
     * https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector
     * @param normal
     * @return returns a copy of this vector after it is reflected by a normal
     */
    public Vector2 reflected(Vector2 normal) {
        Vector2 vector = copy();
        double dotProduct = dotProduct(vector, normal.getPerpendicularVector().normalized());
        // vector - 2 * (dotProduct) * normal;
        Vector2 reflection = vector.translate(normal.getPerpendicularVector().normalized().scale(2 * dotProduct).reversed());
        return reflection;
    }

    /**
     * Picture included
     * https://math.stackexchange.com/questions/13261/how-to-get-a-reflection-vector
     * @param normal The normal vector to reflect with respect to
     * @return reflects this vector by a normal and returns the result
     */
    public Vector2 reflect(Vector2 normal) {
        double dotProduct = dotProduct(this, normal.normalized());
        // vector - 2 * (dotProduct) * normal;
        return translate(normal.normalized().scale(2 * dotProduct).reversed());
    }

    /**
     * The perpendicular vector will always have a positive x coordinate
     */
    public Vector2 getPerpendicularVector(){
        InfLine2D line = new InfLine2D(this, Vector2.zeroVector());
        InfLine2D perpendicularLine = line.getPerpendicularLineAtPoint(Vector2.zeroVector());
        return perpendicularLine.secondPosition;
    }

    /**
     *
     * @return a new vector with the same values as the original
     */
    public Vector2 copy() {
        return new Vector2(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public static double dotProduct(Vector2 vector1, Vector2 vector2) {
        return vector1.x * vector2.x + vector1.y * vector2.y;
    }

    public static double crossProductValue(Vector2 vector1, Vector2 vector2){
        double angleBetweenVectors = angleBetween(vector1, vector2);
        return vector1.length() * vector2.length() * Math.sin(angleBetweenVectors);
    }

    /**
     *
     * @param vector1
     * @param vector2
     * @return angle in range (0; Pi)
     */
    public static double angleBetween(Vector2 vector1, Vector2 vector2) {
        double cos = dotProduct(vector1, vector2) / (vector1.length() * vector2.length());
        return Math.acos(cos);
    }
}
