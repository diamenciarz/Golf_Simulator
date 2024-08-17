package gui.shotinput;

import datastorage.Ball;
import visualization.IInput;
import visualization.gameengine.Game;
import gui.GameStateRenderer;
import utility.UtilityClass;
import utility.math.Vector2;

public class MouseInputReader extends BallVelocityInput implements IClickListener {

    public MouseInputReader(IInput game) {
        super(game);
        game.getClickListener().add(this);
    }

    private boolean isListening = false;

    @Override
    public void readyForNextInput() {
        if (isListening) {
            return;
        }
        isListening = true;
        game.getUpdateLoop().drawArrow = true;
    }

    @Override
    public void hideInputWindow() {
    }

    public void mouseWasClicked() {
        if (!isListening) {
            return;
        }
        this.game.getUpdateLoop().setShotForce(getForce(Game.getMiddleMousePosition()));
        isListening = false;
    }

    private synchronized Vector2 getForce(Vector2 deltaPosition) {
        double maxVelocity = Ball.maxSpeed;
        Vector2 clampedMousePosition = UtilityClass.clamp(deltaPosition, 0, GameStateRenderer.MAX_ARROW_LENGTH);

        double forceValue = (clampedMousePosition.length() / GameStateRenderer.MAX_ARROW_LENGTH) * maxVelocity;
        return clampedMousePosition.normalized().scale(forceValue);
    }

    @Override
    public void stopListening() {
        isListening = false;
    }
}
