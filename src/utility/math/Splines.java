package utility.math;

import function.Function;

public class Splines{

    //public double[] point;
    public String function;
    public function.Function f;

    public Splines(String function){
        f = new Function(function);
        function = "h(x,y) = " + f.toString();

        /*
        for(int i = 0; i < point.length; i++){

            point[i] = Double.valueOf(point[i]);

        } */

    }

    public double derivativeAt(double[] point){

        double xy = 0;

        for(int i = 0; i < point.length; i++){

            xy = f.evaluate(new String[] { "x", "y" }, new double[] { point[i], point[i+1] });

        }

        return xy;
    }

    /**
     *
     public double derivativeAtX(double[] pointx, double y){

     double x = 0;

     // public function.Function fx;
     for(int i = 0; i < pointx.length; i++){

     x = fx.evaluate(new String[] { "x", "y" }, new double[] { pointx[i], point[y] });

     }

     return x;
     }

     *
     public double derivativeAtY(double x, doule[] pointy){

     double y = 0;

     //public function.Function fy;
     for(int i = 0; i < pointy.length; i++){

     y = fy.evaluate(new String[] { "x", "y" }, new double[] { point[x], pointy[i] });

     }

     return y;
     }

     *
     public double derivativeAtXY(double[] pointx, doule[] pointy){

     double xy = 0;

     //public function.Function fxy;
     for(int i = 0; i < pointx.length; i++){
     for(int j = 0; j < pointy.length; j++){

     xy = fxy.evaluate(new String[] { "x", "y" }, new double[] { point[i], point[j] });

     }
     }

     return xy;
     }

     */


    public String toString() {
        return function;
    }



}
