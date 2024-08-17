package bot.botimplementations;

import datastorage.GameState;
import utility.math.Vector2;

public interface IBot {
    public Vector2 findBestShot(GameState gameState);
    public int getNumSimulations();
    public int getNumIterations();
}
