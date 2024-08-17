package visualization.jmonkeyrender;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

public class TerrainEditor {
    private Renderer renderer;
    private BitmapText ch;
    private boolean flyCam;

    public TerrainEditor(Renderer renderer) {
        this.renderer = renderer;
        this.flyCam = true;

        switchCamera();
        initCrossHair();
    }

    public void switchCamera(){
        renderer.getFlyByCamera().setEnabled(flyCam);
        renderer.chaseCam.setEnabled(!flyCam);
        renderer.getFlyByCamera().setDragToRotate(!flyCam);
        renderer.getFlyByCamera().setMoveSpeed(50);

        if(!flyCam) renderer.getGuiNode().detachChild(ch);

        flyCam = !flyCam;
    }

    protected void initCrossHair() {
        BitmapFont guiFont = renderer.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                (float) renderer.WIDTH / 2 - ch.getLineWidth()/2,
                (float) renderer.HEIGHT / 2 + ch.getLineHeight()/2, 0);
        renderer.getGuiNode().attachChild(ch);
    }
}
