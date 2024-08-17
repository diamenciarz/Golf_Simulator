package visualization.jmonkeyrender;

import com.jme3.app.SimpleApplication;
import com.jme3.input.ChaseCamera;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import gui.MenuGUI;

import gui.shotinput.IClickListener;
import reader.GameStateLoader;
import utility.math.Vector2;
import datastorage.*;

import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import visualization.IInput;
import visualization.UpdateLoop;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Renderer extends SimpleApplication implements IInput {
    //Stores all the obstacles that will be projected onto the screen
    protected Node obstacles = new Node("Obstacles");

    protected int WIDTH = 1280;
    protected int HEIGHT = 720;

    private MapGeneration mapGeneration;
    protected ObjectGeneration objectGeneration;
    private UIGeneration uiGeneration;
    private Inputs inputsGenerator;
    private final Cam camInit = new Cam();

    private UpdateLoop updateLoop;

    private TerrainQuad terrainQuad;
    private GameState gameState;
    private Geometry ballRender;
    private Geometry arrowRender = new Geometry("Arrow");
    private Vector2f shotInput = new Vector2f(0,0);

    //Stores points that are rendered on to the screen with terrain editor
    ArrayList<Geometry> pointRenders = new ArrayList<>();

    protected ChaseCamera chaseCam;

    protected static final float ballRadius = 2.56f;

    private Terrain terrain;

    protected Ball ball;

    private BitmapText text;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    private float normalFactor;
    private final float terScale = 6;
    private float pixelScale;

    protected ArrayList<IClickListener> clickListeners = new ArrayList<>();

    public UpdateLoop getUpdateLoop() {
        return updateLoop;
    }

    @Override
    public ArrayList<IClickListener> getClickListener() {
        return clickListeners;
    }

    public GameState getGameState() {
        return gameState;
    }

    public float getTerScale() {
        return terScale;
    }

    public float getNormalFactor() {
        return normalFactor;
    }

    public float getPixelScale(){
        return pixelScale;
    }

    public float getBallRadius() {
        return ballRadius;
    }

    public void setTerrainQuad(TerrainQuad terrainQuad) {
        this.terrainQuad = terrainQuad;
    }

    public void setText(BitmapText text) {
        this.text = text;
    }

    public void setBallRender(Geometry ballRender) {
        this.ballRender = ballRender;
    }

    public Vector2f getShotInput() {
        return shotInput;
    }

    public Inputs getInputsGenerator() {
        return inputsGenerator;
    }

    /**
     * Moves the by finding the normal tangent by radius of the ball
     * so it would not be that ball is in the terrain
     */
    public void findTangent(Vector2 ballState){
        Vector3f terNormal = terrainQuad.getNormal(new Vector2f((float)ballState.x*pixelScale, (float)ballState.y*pixelScale));
        double scalar = ballRadius/terNormal.length();
        terNormal = terNormal.mult((float) scalar);
        ballRender.move(terNormal.x, terNormal.y, terNormal.z);
    }

    /**
     * Moves ball according to x & y coordinates
     */
    public void moveBall(Vector2 ballState){
        if(ballState.x*pixelScale<(float)(terrain.getVERTECES_PER_SIDE()-1)/2 && ballState.y*pixelScale < (float)(terrain.getVERTECES_PER_SIDE())/2) {
            //Getting height value corresponding to x and y values
            float val = terrain.heightMapValueAt(ballState)*terScale;

            //Moving the ball object to specified position
            ballRender.setLocalTranslation((float) (ballState.x)*pixelScale, val, (float) (ballState.y)*pixelScale);
            //Adjusting the ball not to be in the ground
            findTangent(ballState);

            //Outputting the position of the ball
            text.setText("x: " + df.format(ballState.x) + "  y: " + df.format(ballState.y) + "  z: "+ df.format(val/terScale));

            //Displaying minimap based on the ball position
            uiGeneration.generateMinimap(ballState);
        }
    }

    public void drawPoint(Vector3f pointLoc){
        Sphere lookingPoint = new Sphere(120, 120, 2);
        Geometry pointRender = new Geometry("Point" + pointRenders.size(), lookingPoint);

        //Adding textures to the ball
        Material mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        pointRender.setMaterial(mat);

        pointRender.setLocalTranslation(pointLoc.x, pointLoc.y, pointLoc.z);

        pointRenders.add(pointRender);

        //add the geometry object to the scene
        for(Geometry point: pointRenders){
            obstacles.attachChild(point);
        }
        getRootNode().attachChild(obstacles);
    }

    public void drawArrow(){
        shotInput = inputManager.getCursorPosition();
        //Making vector around the center of the screen
        shotInput = shotInput.add(new Vector2f(-WIDTH/2, -HEIGHT/2)).mult(0.1f);
        //Rotating the vector based on the camera angle
        shotInput.rotateAroundOrigin(getCamera().getRotation().toAngles(null)[1],false);
        makeArrow(shotInput);
    }


    public void makeArrow(Vector2f cursorPos) {
        //Removing previous render of arrow from the scene
        getRootNode().detachChild(arrowRender);
        //Limiting the length of to 15 game units
        if(cursorPos.length() > 15)cursorPos = cursorPos.normalize().mult(15);
        arrowRender = objectGeneration.initArrow(cursorPos);
        getRootNode().attachChild(arrowRender);
    }

    public void generateWorld(){
        getRootNode().attachChild(obstacles);
        mapGeneration.initMap(MenuGUI.texPath);
        objectGeneration.initObjects();
        uiGeneration.initText(guiFont);
    }

    /**
     * Initializes objects that will generate world
     */
    public void initPointers(){
        mapGeneration = new MapGeneration(this);
        objectGeneration = new ObjectGeneration(this);
        uiGeneration = new UIGeneration(this);
        inputsGenerator = new Inputs(this);
        updateLoop = new UpdateLoop(gameState);
    }
    /**
     * Initializes physics for calculating the ball movement
     */
    public void initPhysics(){
        //Attaches the input values to Terrain object
        this.gameState = GameStateLoader.readFile();
        this.terrain = gameState.getTerrain();
        this.normalFactor = (float) terrain.NORMAL_FACTOR;
        this.pixelScale = (float) terrain.getVERTECES_PER_SIDE()/100;

        this.ball = this.gameState.getBall();
    }

    @Override
    public void simpleInitApp() {
        //Disabling unnecessary information and commands
        inputManager.deleteMapping( SimpleApplication.INPUT_MAPPING_MEMORY );
        setDisplayStatView(false);

        //Initializing simulation
        initPhysics();
        initPointers();
        generateWorld();
        moveBall(this.ball.state.position);

        //Setting controls for the simulation
        inputsGenerator.initKeys();
        inputsGenerator.mouseInput();

        //creating and attaching camera to ball
        chaseCam = new ChaseCamera(cam, ballRender, inputManager);
        camInit.InitCam(chaseCam,this);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //simulates from Vectors.csv file
        //moves the ball with calculated position
        updateLoop.updateLoop();
        //While the user's input is through mouse, it will draw an arrow
        if(inputsGenerator.isMouseInput() && !inputsGenerator.isTerrainEditor())drawArrow();
        else getRootNode().detachChild(arrowRender);
        //Simulation of shot
        if(updateLoop.getBallPositions().size() != 0) {
            getRootNode().detachChild(arrowRender);
            gameState.setBallPosition(updateLoop.getBallPositions().get(0));
            moveBall(ball.state.position);
            updateLoop.getBallPositions().remove(0);
        }

    }

    public void start3d(){
        this.setShowSettings(false);
        // Setting up renderer settings, so JME settings tab wouldn't pop out
        AppSettings settings = new AppSettings(true);
        settings.put("Width", WIDTH);
        settings.put("Height", HEIGHT);
        settings.put("Title", "Golf Game");
        settings.put("VSync", true);
        settings.put("Samples", 4);
        this.setSettings(settings);

        this.start();
    }

    public void clearPoint() {
        for(Geometry point: pointRenders){
            obstacles.detachChild(point);
        }
        pointRenders = new ArrayList<>();
    }

    public void removeObject(Geometry geometry) {
        //Checking for the geometry not to be a terrain object
        if(!geometry.getName().contains("Course")){
            String geometryName = geometry.getName();
            int obsID = Integer.parseInt(String.valueOf(geometryName.charAt(geometryName.length()-1)));
            String obsType = geometryName.substring(0,geometryName.length()-1);
            terrain.removeObstacleAt(obsID,obsType);
            obstacles.detachChild(geometry);
        }
        uiGeneration.updateMinimap();
    }

    public void drawObstacle(String obstacleType, Vector3f start, Vector3f end){
        if(end!=null) {
            if (start.x > end.x && start.z > end.z) {
                Vector3f hold = start.clone();
                start.z = end.z;
                end.z = hold.z;
            }
            if (start.x < end.x && start.z < end.z) {
                Vector3f hold = start.clone();
                start.z = end.z;
                end.z = hold.z;
            }
        }
        obstacles.attachChild(objectGeneration.drawObstacle(obstacleType, start, end,-1));
        uiGeneration.updateMinimap();
    }

    public Terrain getTerrain() {
        return gameState.getTerrain();
    }
}