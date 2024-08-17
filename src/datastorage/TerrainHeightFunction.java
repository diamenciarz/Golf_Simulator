package datastorage;

import function.Function;
// import org.mariuszgromada.math.mxparser.*;
import utility.math.Vector2;

public class TerrainHeightFunction extends TerrainHeight {
    public function.Function f;
    public function.Function dfx;
    public function.Function dfy;

    public TerrainHeightFunction(String function) {
        f = new Function(function);
        dfx = f.getDerivative("x");
        dfy = f.getDerivative("y");
    }

    @Override
    public double valueAt(double x, double y) {
        double value = f.evaluate(new String[] { "x", "y" }, new double[] { x, y });
        return value;
    }


    @Override
    public double xDerivativeAt(double x, double y) {
        return dfx.evaluate(new String[] { "x", "y" }, new double[] { x, y });
    }

    @Override
    public double yDerivativeAt(double x, double y) {
        return dfy.evaluate(new String[] { "x", "y" }, new double[] { x, y });
    }

    @Override
    public String toString() {
        return "h(x,y) = " + f.toString() + "\n" +
                "dh/dx = " + dfx.toString() + "\n" +
                "dh/dy = " + dfy.toString();
    }

    @Override
    public double valueAt(Vector2 position) {
        return valueAt(position.x, position.y);
    }

    @Override
    public double xDerivativeAt(Vector2 position) {
        return xDerivativeAt(position.x, position.y);
    }

    @Override
    public double yDerivativeAt(Vector2 position) {
        return yDerivativeAt(position.x, position.y);
    }
}
