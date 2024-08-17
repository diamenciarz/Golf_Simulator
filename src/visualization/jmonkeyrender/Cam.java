package visualization.jmonkeyrender;

import com.jme3.input.ChaseCamera;

public class Cam{
    /**
     * Creating Chase camera
     * @param chaseCam object that follows to a player/ball
     */
    public void InitCam(ChaseCamera chaseCam, Renderer renderer) {
        renderer.getFlyByCamera().setEnabled(false);               // disable the default first-person camera
        chaseCam.setSmoothMotion(true);
        chaseCam.setDefaultDistance(40f);       // default distance to the target (ball)
        chaseCam.setChasingSensitivity(100);     // the lower the sensitivity the slower the camera will follow the target when it moves
        chaseCam.setRotationSensitivity(40);
        chaseCam.setMaxDistance(500);
        chaseCam.setMinDistance(10f);
    }



}