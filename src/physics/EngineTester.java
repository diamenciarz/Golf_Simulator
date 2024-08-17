package physics;

import function.Function;
import physics.collisionsystems.StopCollisionSystem;
import physics.solvers.EulerSolver;
import physics.solvers.RungeKutta2Solver;
import physics.solvers.RungeKutta4Solver;
import physics.stoppingconditions.SmallVelocityStoppingCondition;
import utility.math.Vector2;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import datastorage.Ball;
import datastorage.Terrain;

public class EngineTester {
    public String testStepSizeAccuracy(PhysicsEngine engine, int numStepSizes, double stopT, Vector2 v0, Vector2 p0, Terrain terrain, ArrayList<Vector2> actualPositions, double actualH) {
        String data = "";

        Ball ball = new Ball(p0, Vector2.zeroVector());

        // Test all the other engines
        for (int n=numStepSizes; n>0; n--) {
        //for (int n=0; n<numStepSizes; n++) {
            System.out.print("#");
            double h = stopT / n;//(10000 - 10000 / numStepSizes * n);
            engine.odeSolver.setStepSize(h);
            ArrayList<Vector2> shotPositions = engine.simulateShot(v0, ball, terrain);
            double t = 0;
            Vector2 position = null;
            Vector2 actualPosition = null;
            // Find the simulated value
            double diff = stopT;
            for (int i = 0; i < shotPositions.size(); i++) {
                t = i * h;
                double diffNew = Math.abs(stopT - t);
                if (diffNew > diff) {//if (t >= stopT) {
                    position = shotPositions.get(i - 1);
                    break;
                }
                diff = diffNew;
            }
            // Find the actual value
            diff = stopT;
            for (int i = 0; i < actualPositions.size(); i++) {
                double tActual = i * actualH;
                double diffNew = Math.abs(stopT - tActual);
                if (diffNew > diff) {//if (tActual >= t) {
                    actualPosition = actualPositions.get(i-1);
                    break;
                }
                diff = diffNew;
            }
            // Calculate the error
            double error = actualPosition.distanceTo(position);
            // Store the error
            data += h + ", " + Math.log10(h) + ", " + error + ", " + Math.log10(error) + "\n";
        }
        System.out.println("\n");
        return data;
    }

    public String testAllEngines(Terrain terrain, Vector2 v0, Vector2 p0, double stopT, int numStepSizes) {
        double actualH = stopT/(4*numStepSizes);
        PhysicsEngine rk4 = new PhysicsEngine(
                new RungeKutta4Solver(actualH),
                new SmallVelocityStoppingCondition(),
                new StopCollisionSystem()
        );
        PhysicsEngine euler = new PhysicsEngine(
                new EulerSolver(actualH),
                new SmallVelocityStoppingCondition(),
                new StopCollisionSystem()
        );
        PhysicsEngine rk2 = new PhysicsEngine(
                new RungeKutta2Solver(actualH),
                new SmallVelocityStoppingCondition(),
                new StopCollisionSystem()
        );
        Ball ball = new Ball(p0, v0);
        ArrayList<Vector2> actualPositions = rk4.simulateShot(v0, ball, terrain);

        double t=0;
        int i=0;
        while (t < stopT) {
            t += actualH;
            i++;
        }
        Vector2 actualPosition = actualPositions.get(i);

        System.out.println(actualPosition);
        System.out.println(actualPositions.get(actualPositions.size()-1));
        System.out.println("Time to stop: "+actualPositions.size()*actualH);

        String eulerData = testStepSizeAccuracy(
                euler,
                numStepSizes,
                stopT,
                v0,
                p0,
                terrain,
                actualPositions,
                actualH
        );

        String rk2Data = testStepSizeAccuracy(
                rk2,
                numStepSizes,
                stopT,
                v0,
                p0,
                terrain,
                actualPositions,
                actualH
        );

        String rk4Data = testStepSizeAccuracy(
                rk4,
                numStepSizes,
                stopT,
                v0,
                p0,
                terrain,
                actualPositions,
                actualH
        );

        String data = "euler, , , , rk2, , , , rk4 , , ,\n";
        data += "h, log(h), e, log(e), h, log(h), e, log(e), h, log(h), e, log(e)\n";

        String[] eulerDataSplit = eulerData.split("\n");
        String[] rk2DataSplit = rk2Data.split("\n");
        String[] rk4DataSplit = rk4Data.split("\n");

        for (int line=0; line<eulerDataSplit.length; line++) {
            data += eulerDataSplit[line]+","+rk2DataSplit[line]+","+rk4DataSplit[line]+"\n";
        }

        return data;
    }

    public void saveTestData(String data, String fileName) {
        try {
            File f = new File(System.getProperty("user.dir")+"/Phase 1/src/physics/results/"+fileName+".csv");
            FileWriter fw = new FileWriter(f);
            fw.append(data);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //PhysicsEngine engine = new PhysicsEngine(new RungeKutta4Solver(0.01), new SmallVelocityStoppingCondition(), new StopCollisionSystem());
        EngineTester et = new EngineTester();
        /*String data = et.testStepSizeAccuracy(
                engine,
                10,
                0.2,
                new Vector2(3, 0),
                new Vector2(-1, -0.5),
                new Terrain(
                        "e**(-(x*x + y*y)/40)",
                        0.2,
                        0.1,
                        new Vector2(-50, -50),
                        new Vector2(50, 50)
                ),
                new ArrayList<Vector2>(),
                0.2/40
        );*/
        String data = et.testAllEngines(
                new Terrain(
                        "e**(-(x*x+y*y)/8)",
                        0.2,
                        0.1,
                        new Vector2(-50, -50),
                        new Vector2(50, 50)
                ),
                new Vector2(3, 0),
                new Vector2(-1, 0.5),
                0.1,
                100
        );
        et.saveTestData(data, "solvers-"+System.nanoTime());
    }
}
