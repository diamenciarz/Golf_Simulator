package datastorage;

import utility.math.Vector2;

public abstract class TerrainHeight {
    public abstract double valueAt(double x, double y);

    public abstract double valueAt(Vector2 position);

    public abstract double xDerivativeAt(double x, double y);

    public abstract double xDerivativeAt(Vector2 position);

    public abstract double yDerivativeAt(double x, double y);
    
    public abstract double yDerivativeAt(Vector2 position);

}
