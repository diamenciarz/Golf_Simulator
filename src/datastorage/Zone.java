package datastorage;

import utility.math.Rectangle;
import utility.math.Vector2;

public class Zone extends Rectangle {

    public double staticFriction = 0.4;
    public double kineticFriction = 0.3;

    public Zone() {}

    public Zone(Vector2 bottomLeftCorner, Vector2 topRightCorner) {
        super(bottomLeftCorner, topRightCorner);
    }

    public double getStaticFriction() {
        return staticFriction;
    }

    public double getKineticFriction() {
        return kineticFriction;
    }
    public void print(){
        System.out.println("Zone: ");
        System.out.print("Down left corner: ");
        System.out.println(bottomLeftCorner);
        System.out.print("Top right corner: ");
        System.out.println(topRightCorner);
        System.out.print("Static friction: ");
        System.out.println(getStaticFriction());
        System.out.print("Kinetic friction: ");
        System.out.println(getKineticFriction());
    }
}
