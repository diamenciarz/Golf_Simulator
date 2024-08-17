package visualization.jmonkeyrender;

import bot.botimplementations.*;
import bot.heuristics.FinalAStarDistanceHeuristic;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.math.Ray;
import gui.shotinput.MouseInputReader;
import gui.shotinput.ShotInputWindow;
import utility.math.Vector2;

import java.util.ArrayList;

public class Inputs {
    private final Renderer renderer;
    private boolean isMouseInput= true;
    protected static boolean isTerrainEditor = false;
    private String obstacleType = "Box";
    private TerrainEditor terrainEditor;
    private ArrayList<CollisionResults> collisions = new ArrayList<>();

    public Inputs(Renderer renderer) {
        this.renderer = renderer;
    }

    public boolean isMouseInput() {
        return isMouseInput;
    }

    public boolean isTerrainEditor() {
        return isTerrainEditor;
    }

    public void setTerrainEditor(boolean terrainEditor){
        isTerrainEditor = terrainEditor;
    }

    public void setObstacleType(String obstacleType) {
        this.obstacleType = obstacleType;
    }

    public void setMouseInput(boolean mouseInput) {
        isMouseInput = mouseInput;
    }

    protected void initKeys() {
        //Mapping each bot implementation to keys
        renderer.getInputManager().addMapping("HC Bot",  new KeyTrigger(KeyInput.KEY_H));
        renderer.getInputManager().addMapping("PS Bot",   new KeyTrigger(KeyInput.KEY_P));
        renderer.getInputManager().addMapping("GD Bot",  new KeyTrigger(KeyInput.KEY_G));
        renderer.getInputManager().addMapping("Rule Bot", new KeyTrigger(KeyInput.KEY_B));
        renderer.getInputManager().addMapping("AStar Bot", new KeyTrigger(KeyInput.KEY_A));
        renderer.getInputManager().addMapping("Manual Input", new KeyTrigger(KeyInput.KEY_L));
        renderer.getInputManager().addMapping("Change Input Type", new KeyTrigger(KeyInput.KEY_K));
        renderer.getInputManager().addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));
        renderer.getInputManager().addMapping("Terrain Editor", new KeyTrigger(KeyInput.KEY_Z));
        renderer.getInputManager().addMapping("Box", new KeyTrigger(KeyInput.KEY_1));
        renderer.getInputManager().addMapping("Tree", new KeyTrigger(KeyInput.KEY_2));

        //Setting listeners for inputs
        renderer.getInputManager().addListener(keyListener, "PS Bot","HC Bot","GD Bot","Rule Bot","AStar Bot","Manual Input","Reset", "Change Input Type", "Terrain Editor", "Box", "Tree");
    }
    private final ActionListener keyListener = new ActionListener() {
        public void resetGame(){
            renderer.getUpdateLoop().resetGame();
            renderer.moveBall(renderer.ball.state.position);
        }

        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("HC Bot") && !keyPressed && !isTerrainEditor) {
                renderer.getUpdateLoop().setBot(BotFactory.getBot(BotFactory.BotImplementations.HILL_CLIMBING));
            }
            if (name.equals("PS Bot") && !keyPressed && !isTerrainEditor) {
                renderer.getUpdateLoop().setBot(BotFactory.getBot(BotFactory.BotImplementations.PARTICLE_SWARM));
            }
            if (name.equals("GD Bot") && !keyPressed && !isTerrainEditor) {
                renderer.getUpdateLoop().setBot(BotFactory.getBot(BotFactory.BotImplementations.GRADIENT_DESCENT));
            }
            if (name.equals("Rule Bot") && !keyPressed && !isTerrainEditor) {
                renderer.getUpdateLoop().setBot(BotFactory.getBot(BotFactory.BotImplementations.RULE));
            }
            if(name.equals("AStar Bot") && !keyPressed && !isTerrainEditor){
                renderer.getUpdateLoop().setBot(BotFactory.getBot(BotFactory.BotImplementations.PARTICLE_SWARM));
            }
            if (name.equals("Manual Input") && !keyPressed && !isTerrainEditor) {
                renderer.getUpdateLoop().setBot(null);
                resetGame();
                System.out.println("Manual Input");
            }
            if(name.equals("Change Input Type") && !keyPressed && !isTerrainEditor){
                if(isMouseInput){
                    renderer.getUpdateLoop().setManualInputType(new ShotInputWindow(renderer));
                }else{
                    renderer.getUpdateLoop().getBallVelocityInput().hideInputWindow();
                    renderer.getUpdateLoop().setManualInputType(new MouseInputReader(renderer));
                }
                isMouseInput = !isMouseInput;
                System.out.println("Changed Input");
            }
            if (name.equals("Reset") && !keyPressed && !isTerrainEditor) {
                resetGame();
                System.out.println("Game Reset");
            }
            if(name.equals("Terrain Editor")&& !keyPressed){
                if(!isTerrainEditor){
                    isMouseInput = false;
                    System.out.println("Terrain editor mode on");
                    System.out.println("Object Type: "+ obstacleType);

                    terrainEditor = new TerrainEditor(renderer);

                }else{
                    System.out.println("Terrain editor mode off");
                    isMouseInput = true;

                    terrainEditor.switchCamera();
                }
                isTerrainEditor = !isTerrainEditor;
            }
            if(name.equals("Box") && !keyPressed){
                obstacleType = name;
                System.out.println("Object Type: "+ obstacleType);
            }
            if(name.equals("Tree") && !keyPressed){
                obstacleType = name;
                System.out.println("Object Type: "+ obstacleType);
            }
        }
    };

    protected void mouseInput(){
        renderer.getInputManager().addMapping("Left Click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        renderer.getInputManager().addMapping("Remove Object" , new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

        renderer.getInputManager().addListener(mouseListener, "Left Click", "Remove Object");

        renderer.getUpdateLoop().setManualInputType(new MouseInputReader(renderer));
    }

    private final ActionListener mouseListener = new ActionListener() {
        private CollisionResults getCollisions(String collision){
            CollisionResults collisionResults = new CollisionResults();
            Ray ray = new Ray(renderer.getCamera().getLocation(), renderer.getCamera().getDirection());
            renderer.getRootNode().getChild(collision).collideWith(ray,collisionResults);
            return collisionResults;
        }

        @Override
        public void onAction(String name, boolean clicked, float v) {
            if(name.equals("Left Click") && !clicked && isMouseInput && renderer.getUpdateLoop().isSimulationFinished()){
                renderer.getUpdateLoop().setShotForce(new Vector2(-renderer.getShotInput().getX()/15*5,renderer.getShotInput().getY()/15*5));
            }
            if(name.equals("Left Click") && !clicked && isTerrainEditor){
                collisions.add(getCollisions("Course"));
                renderer.drawPoint(collisions.get(collisions.size() - 1).getCollision(0).getContactPoint());

                //The box would be made if the size is bigger than 2
                if (collisions.size() == 2 || (collisions.size()==1 && obstacleType.equals("Tree"))) {
                    if(obstacleType.equals("Box")) renderer.drawObstacle(obstacleType, collisions.get(0).getCollision(0).getContactPoint(),
                            collisions.get(1).getCollision(0).getContactPoint());
                    else renderer.drawObstacle(obstacleType, collisions.get(0).getCollision(0).getContactPoint(), null);
                    renderer.clearPoint();
                    collisions = new ArrayList<>();
                }
            }
            if(name.equals("Remove Object") && !clicked && isTerrainEditor){
                CollisionResults collisionResults = getCollisions("Obstacles");
                if(collisionResults.size()>0) renderer.removeObject(collisionResults.getCollision(0).getGeometry());
            }
        }
    };
}
