package visualization.jmonkeyrender;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.ui.Picture;
import visualization.gameengine.Camera;
import gui.GameStateRenderer;
import utility.math.Vector2;

import java.awt.image.BufferedImage;

public class UIGeneration {
    private Renderer renderer;
    private final GameStateRenderer minimapGenerator;
    private final AWTLoader loader = new AWTLoader();
    private final Picture pic = new Picture("HUD Picture");
    private final Texture2D texture2D = new Texture2D();

    public UIGeneration(Renderer renderer) {
        this.renderer = renderer;
        minimapGenerator = new GameStateRenderer(renderer.getGameState());
    }

    public void updateMinimap(){
        minimapGenerator.updateTerrain();
        generateMinimap(renderer.getGameState().getBall().state.position);
    }

    public void generateMinimap(Vector2 ballState){
        Camera camera = new Camera(30,30);
        camera.xPos = ballState.x;
        camera.yPos = ballState.y;

        BufferedImage minimapImg = minimapGenerator.getMinimap(camera);
        Image img = loader.load(minimapImg, true);
        texture2D.setImage(img);
        pic.setTexture(renderer.getAssetManager(), texture2D, true);
        pic.setHeight(300);
        pic.setWidth(300);
        pic.setPosition(1280-300, 720-300);
        renderer.getGuiNode().attachChild(pic);
        img.dispose();
        minimapImg.flush();
    }

    /**
     * Displays text with current ball position
     */
    public void initText(BitmapFont guiFont){
        BitmapText hudText = new BitmapText(guiFont);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setColor(ColorRGBA.White);
        hudText.setText("");
        hudText.setLocalTranslation(0, 50, 0);
        renderer.setText(hudText);
        renderer.getGuiNode().setQueueBucket(RenderQueue.Bucket.Gui);
        renderer.getGuiNode().attachChild(hudText);

    }
}
