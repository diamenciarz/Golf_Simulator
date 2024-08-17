/**
 * In this class the general rules and conditions for the bot to play the simple putting game are defined.
 * Input:
 * - terrain function
 * - target position
 * - ball position
 * - obstacles
 *   Basically the whole geometry of the current state
 */
package bot.botimplementations;
import datastorage.*;
import datastorage.obstacles.IObstacle;
import utility.math.*;

public class RuleBasedBot implements IBot{

    private Terrain terrain;
    private Ball ball;
    private Vector2 targetPos;
    private double sFriction;
    private double kFriction;
    private final double maxSpeed = 5.0;
    private Vector2 distance;
    private Vector2 speed;
    private double dotProduct;
    private double normX, normY;
    private boolean hill;
    private boolean upHill = false;
    private boolean downHill = false;
    private double ballPosX;
    private double ballPosY;
    private Vector2 ballPos;
    private double tarPosX;
    private double tarPosY;
    private double maxSlopeX = Double.MIN_VALUE;
    private double maxSlopeY = Double.MIN_VALUE;
    private double minSlopeX = Double.MAX_VALUE;
    private double minSlopeY = Double.MAX_VALUE;
    private Rectangle rec = new Rectangle();
    private int numSimulations;

    private void setup(GameState gameState) {
        this.terrain = gameState.getTerrain();
        this.sFriction = terrain.staticFriction;
        this.kFriction = terrain.staticFriction;
        this.targetPos = terrain.target.position;
        this.ball = gameState.getBall();
        // POSITION VALUES, i.e. current x-position of ball
        this.ballPosX = ball.state.position.x;
        this.ballPosY = ball.state.position.y;
        this.tarPosX = terrain.target.position.x;
        this.tarPosY = terrain.target.position.y;
        this.ballPos = new Vector2(this.ballPosX, this.ballPosY);
    }

    // HELPER METHODS
    /**
     * compute distance between current ball position and target position in the terrain
     */
    public Vector2 computeDistance(){
        double distanceX = tarPosX - ballPosX;
        double distanceY = tarPosY - ballPosY;
        return distance = new Vector2(distanceX, distanceY);
    }

    public Vector2 getNormalizedDistance(Vector2 distance){
        return distance.normalized();
    }

    public void findHill(){
        hill = false;
        int positive_Derivative = 0;
        int negative_Derivative = 0;
        int zero_Derivative = 0;


        // for every x in between ball and target x-position (along the distance vector)
        for (double x = ballPosX,  y = ballPosY; x <= tarPosX && y <= tarPosY; x++, y++){

            // if HILL is nearby, adjust velocity or shoot around it // consider level of STEEPNESS
            Vector2 slope = new Vector2(terrain.terrainFunction.xDerivativeAt(x, y), terrain.terrainFunction.yDerivativeAt(x, y));
            if (slope.x > 0 && slope.y > 0) {
                positive_Derivative++;
                hill = true;
            }
            if (slope.x < 0 && slope.y < 0) {
                negative_Derivative++;
                hill = true;
            }
            if (slope.x == 0 && slope.y == 0) {
                zero_Derivative++;
            }
            /* // get largest derivative, so steepest point in uphill slope
             if (slope.x >= maxSlopeX && slope.y >= maxSlopeY){
             maxSlopeX = slope.x;
             maxSlopeY = slope.y;
             //maxSlope = new Vector2(maxSlopeX, maxSlopeY);
             }
             // get smallest derivative, so steepest point in downhill slope
             if (slope.x <= minSlopeX && slope.y <= maxSlopeY){
             minSlopeX = slope.x;
             minSlopeY= slope.y;
             //minSlope = new Vector2(minSlopeX, minSlopeY);
             } */
        }
        if (positive_Derivative > negative_Derivative && positive_Derivative > zero_Derivative){
            upHill = true;
        }
        if (negative_Derivative > positive_Derivative && negative_Derivative > zero_Derivative){
            downHill = true;
        }
        if (zero_Derivative > negative_Derivative && zero_Derivative > positive_Derivative){
            hill = false;
        }
    }

    public Vector2 findBestShot(GameState gameState){
        numSimulations = 0;
        gameState = gameState.copy();
        setup(gameState);
        findHill();
        // return a velocity vector
        distance = computeDistance();
        double initalSpeed = 2.5;
        double speedMargin = 1;     // a constant by which the speed is conditionally increased
        double threshold = 0.1;
        double angle = 15.0;
        normX = getNormalizedDistance(distance).x;
        normY = getNormalizedDistance(distance).y;

        speed = new Vector2(normX * initalSpeed, normY * initalSpeed);
        // if OBSTACLE is nearby (i.e. sandpit on the slope, consider correct friction in this case)
        Vector2 slope;

        for (double x = ballPosX,  y = ballPosY; x <= tarPosX && y <= tarPosY; x+=normX, y+=normY){
            boolean inObstacle = false;
            for (IObstacle obstacle : terrain.obstacles) {
                if (obstacle.isPositionColliding(ballPos)) {
                    inObstacle = true;
                    break;
                }
            }
            //THIS IS STILL TO BE COMPLETED
            if (inObstacle){          // checking for obstacles other than water
                // rotate speed vector by 15 degrees angle
                // rotated as follows: x2 = cos (𝛽𝑥1) − sin(𝛽𝑦) and y2 = sin(𝛽𝑦) + cos (𝛽𝑥1)
                speed = new Vector2(Math.cos(angle*speed.x) - Math.sin(angle*speed.y), Math.sin(angle*speed.x) + Math.cos(angle*speed.y));

            }
            if (ball.state.getZCoordinate(terrain) < 0){     // checking for water
                // rotate speed vector s.t. the new vector shoots around the water
            }
        }

        for (double x = ballPosX,  y = ballPosY; x <= tarPosX && y <= tarPosY; x+=normX, y+=normY){

            // take derivative at current point on the line and dot product of speed and derivative
            slope = new Vector2(terrain.terrainFunction.xDerivativeAt(x, y), terrain.terrainFunction.yDerivativeAt(x, y));
            dotProduct = Vector2.dotProduct(speed, slope);

            // if LONG distance
            if (distance.length() > 5){
                //System.out.println("*** SLOPE-VALUES: x = "+ slope.x+"; y = "+slope.y+" ***");
                if (hill){
                    if (dotProduct > 0){        // uphill       // !!! changed !!! was dotProduct < 0 before
                        // if easy slope ...
                        if (slope.length() <= threshold){
                            if (speed.x <= maxSpeed-speedMargin && speed.y <= maxSpeed-speedMargin) {
                                speed = new Vector2(speed.x + speedMargin, speed.y + speedMargin);
                                //speed.scale(1.4);
                            }
                        }
                        // if steep slope ...
                        if (slope.length() > threshold){
                            // added IF statement here !!
                            if (speed.x <= maxSpeed-2*speedMargin && speed.y <= maxSpeed-2*speedMargin) {
                                speed = new Vector2(speed.x + 2* speedMargin, speed.y + 2*speedMargin);
                                //speed.scale(1.4);
                            }
                            else speed = new Vector2(maxSpeed, maxSpeed);
                        }
                    }
                    if (dotProduct < 0){        // downhill
                        // if easy slope ...
                        if (slope.length() <= threshold){
                            if (speed.x >= -maxSpeed + 0.5 * speedMargin && speed.y >= -maxSpeed + 0.5 * speedMargin){
                                speed = new Vector2(speed.x - 0.5 * speedMargin, speed.y - 0.5 * speedMargin);
                                //speed.scale(1.4);
                            }
                        }
                        // if steep slope ...
                        if (slope.length() > threshold){
                            if (speed.x >= -maxSpeed + speedMargin && speed.y >= -maxSpeed + speedMargin) {
                                speed = new Vector2(initalSpeed - speedMargin, initalSpeed - speedMargin);
                                //speed.scale(1.4);
                            }
                        }
                    }
                }
            }
            // if SHORT distance
            if (distance.length() <= 5) {
                if (hill){
                    if (dotProduct > 0){        // uphill      // !!! changed !!!
                        // if easy slope ...
                        if (slope.length() <= threshold){
                            if (speed.x <= maxSpeed-speedMargin && speed.y <= maxSpeed-speedMargin) {
                                speed = new Vector2(speed.x + speedMargin, speed.y + speedMargin);
                                //speed.scale(1.4);
                            }
                        }
                        // if steep slope ...
                        if (slope.length() > threshold){
                            if (speed.x <= maxSpeed-2.2*speedMargin && speed.y <= maxSpeed-2.2*speedMargin) {
                                speed = new Vector2(speed.x + 2.2 * speedMargin, speed.y + 2.2 * speedMargin);
                                //speed.scale(1.4);
                            }
                        }
                    }
                    if (dotProduct < 0){        // downhill
                        // if easy slope ...
                        if (slope.length() <= threshold){
                            if (speed.x >= -maxSpeed + speedMargin && speed.y >= -maxSpeed + speedMargin) {
                                speed = new Vector2(speed.x - 0.5 * speedMargin, speed.y - 0.5 * speedMargin);
                                //speed.scale(1.4);
                            }
                        }
                        // if steep slope ...
                        if (slope.length() > threshold){
                            if (speed.x >= -maxSpeed + speedMargin && speed.y >= -maxSpeed + speedMargin) {
                                speed = new Vector2(initalSpeed - speedMargin, initalSpeed - speedMargin);
                                //speed.scale(1.4);
                            }
                        }
                    }
                }
            }
        }
        return speed;
    }

    @Override
    public int getNumSimulations() {
        return numSimulations;
    }

    @Override
    public int getNumIterations() {
        return 0;
    }

}