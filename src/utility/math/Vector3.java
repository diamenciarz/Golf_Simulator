package utility.math;

public class Vector3 {
    public double x;
    public double y;
    public double z;

    public Vector3() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3(double newX, double newY, double newZ) {
        x = newX;
        y = newY;
        z = newZ;
    }

    // readonly vectors
    public final static Vector3 zeroVector(){ return new Vector3(0, 0, 0);}
    public final static Vector3 rightVector(){ return new Vector3(1, 0, 0);}
    public final static Vector3 leftVector(){ return new Vector3(-1, 0, 0);}
    public final static Vector3 upVector(){ return new Vector3(0, 0, 1);}
    public final static Vector3 downVector(){ return new Vector3(0, 0, -1);}
    public final static Vector3 forwardVector(){ return new Vector3(0, 1, 0);}
    public final static Vector3 backwardVector(){ return new Vector3(0, -1, 0);}
    public final static Vector3 unitVector(){ return new Vector3(1, 1, 1);}

    // Helper methods
    public double length() {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    /**
     * @return this vector after it is translated
     */
    public Vector3 translate(Vector3 vector) {
        x += vector.x;
        y += vector.y;
        z += vector.z;
        return this;
    }

    /**
     * @return this vector after it is translated
     */
    public Vector3 translate(double deltaX, double deltaY, double deltaZ) {
        x += deltaX;
        y += deltaY;
        z += deltaZ;
        return this;
    }

    /**
     * @return a tanslated copy of this vector
     */
    public Vector3 translated(Vector3 vector) {
        return new Vector3(x += vector.x, y += vector.y, z += vector.z);
    }

    /**
     * @return a tanslated copy of this vector
     */
    public Vector3 translated(double deltaX, double deltaY, double deltaZ) {
        return new Vector3(x += deltaX, y += deltaY, z += deltaZ);
    }

    public double distanceTo(Vector3 toVector){
        return Math.sqrt((x - toVector.x) * (x - toVector.x) + (y - toVector.y) * (y - toVector.y) + (z - toVector.z) * (z - toVector.z));
    }

    public Vector3 deltaPositionTo(Vector3 toPosition){
        return new Vector3(x - toPosition.x, y - toPosition.y, z - toPosition.z);
    }

    /**
     * reverses this vector and returns the result
     */
    public Vector3 reverse() {
        x *= -1;
        y *= -1;
        z *= -1;
        return this;
    }

    /**
     * reverses a copy of this vector and returns the result without modifying the original vector
     */
    public Vector3 reversed() {
        return new Vector3(-x, -y, -z);
    }

    /**
     * scales this vector and returns the result
     */
    public Vector3 scale(double scale) {
        x*= scale;
        y*= scale;
        z*= scale;
        return this;
    }

    /**
     * scales a copy of this vector and returns the result without modifying the original vector
     */
    public Vector3 scaled(double scale) {
        return new Vector3(x * scale, y * scale, z * scale);
    }

    /**
     * normalizes this vector and returns the result
     */
    public Vector3 normalize() {
        Vector3 normalizedVector = copy().scale(1 / length());
        x = normalizedVector.x;
        y = normalizedVector.y;
        z = normalizedVector.z;

        return this;
    }

    /**
     * normalizes a copy of this vector and returns the result without modifying the original vector
     */
    public Vector3 normalized() {
        Vector3 normalizedVector = copy().scale(1 / length());
        return normalizedVector;
    }

    /**
     * Adds this value to the vector's length. If the value is negative and higher than current length - the vector will go in the opposite direction
     * @param deltaLength
     * @return  this vector after its length was modified
     */
    public Vector3 modifyLength(double deltaLength){
        double currentLength = length();
        scale((currentLength + deltaLength) / currentLength);

        return this;
    }

    /**
     * Adds this value to the vector's length. If the value is negative and higher than current length - the vector will go in the opposite direction
     * @param deltaLength
     * @return  a copy of this vector with modified length
     */
    public Vector3 modifiedLength(double deltaLength){
        Vector3 newVector = copy();
        double currentLength = length();
        newVector.scale((currentLength + deltaLength) / currentLength);

        return newVector;
    }


    /**
     * returns a copy of this vector after it is reflected by a normal
     */
    public Vector3 reflected(Vector3 normal) {
        Vector3 vector = copy();
        double dotProduct = dotProduct(vector, normal.normalized());
        // vector - 2 * (dotProduct) * normal;
        Vector3 reflection = vector.translate(normal.normalized().scale(2 * dotProduct).reversed());
        return reflection;
    }
    /**
     * reflects this vector and returns it afterwards
     */
    public Vector3 reflect(Vector3 normal) {
        double dotProduct = dotProduct(this, normal.normalized());
        // vector - 2 * (dotProduct) * normal;
        translate(normal.normalized().scale(2 * dotProduct).reversed());
        return this;
    }

    /**
     *
     * @return a new vector with the same values as the original
     */
    public Vector3 copy() {
        return new Vector3(x, y, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public static double dotProduct(Vector3 vector1, Vector3 vector2) {
        return vector1.x * vector2.x + vector1.y * vector2.y + vector1.z * vector2.z;
    }

    public static double crossProductValue(Vector3 vector1, Vector3 vector2) {
        double angleBetweenVectors = angleBetween(vector1, vector2);
        return vector1.length() * vector2.length() * Math.sin(angleBetweenVectors);
    }

    /**
     *
     * @param vector1
     * @param vector2
     * @return angle in range (0; Pi)
     */
    public static double angleBetween(Vector3 vector1, Vector3 vector2) {
        double cos = dotProduct(vector1, vector2) / (vector1.length() * vector2.length());
        return Math.acos(cos);
    }

}
