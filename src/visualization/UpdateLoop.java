package visualization;

import bot.botimplementations.IBot;
import datastorage.GameState;
import gui.shotinput.BallVelocityInput;
import utility.math.Vector2;

import java.util.ArrayList;

public class UpdateLoop {
    private final GameState gameState;

    private ArrayList<Vector2> ballPositions = new ArrayList<Vector2>();
    private BallVelocityInput ballVelocityInput;

    public int numShots;
    private Vector2 shotForce;

    private IBot bot = null;
    private Thread botThread;

    public boolean drawArrow = false;

    public UpdateLoop(GameState gameState) {
        this.gameState = gameState;

    }

    public void updateLoop() {
        handleBallInWater();
        handleInput();
        simulateShot();
    }

    // region Ball in water
    private void handleBallInWater() {
        if (isSimulationFinished()) {
            boolean isBallInWater = gameState.getTerrain().getTerrainFunction().valueAt(
                    gameState.getBall().state.position.x,
                    gameState.getBall().state.position.y) < 0;
            if (isBallInWater) {
                resetGame();
            }
        }
    }

    public void resetGame() {
        gameState.getBall().state.position = gameState.getTerrain().ballStartingPosition;
        if (bot != null && botThread.isAlive()) {
            // End the bot thread if it is still running
            try {
                botThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        resetStartingVariables();
    }

    private void resetStartingVariables() {
        numShots = 0;
        shotForce = null;
        ballPositions = new ArrayList<Vector2>();
    }

    // endregion

    private void handleInput() {
        if (isSimulationFinished() && !hasReachedTarget()) {
            if (bot == null) {
                ballVelocityInput.readyForNextInput();
                drawArrow = true;
            } else {
                ballVelocityInput.stopListening();
                resetBotThread();
                botThread.start();
            }
        }
    }

    private void resetBotThread() {
        botThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Calculating shot...");
                Vector2 bestShot = bot.findBestShot(gameState);
                if (bot != null) {
                    shotForce = bestShot;
                    System.out.println("Velocity: " + shotForce);
                    System.out.println("Number of simulations: " + bot.getNumSimulations());
                    System.out.println("Number of iterations: " + bot.getNumIterations());
                }
            }
        });
    }

    private void simulateShot() {
        if (shouldPushBall()) {
            ballPositions = gameState.simulateShot(shotForce);
            numShots++;
            shotForce = null;
            drawArrow = false;
        }
    }

    private boolean shouldPushBall() {
        boolean ballStopped = ballPositions.size() == 0;
        boolean ballHasNotBeenPushed = shotForce != null;
        return ballStopped && ballHasNotBeenPushed && !hasReachedTarget();
    }

    private boolean hasReachedTarget() {
        double distance = gameState.getBall().state.position.distanceTo(gameState.getTerrain().target.position);
        return distance <= gameState.getTerrain().target.radius;
    }

    // region Accessor methods
    public ArrayList<Vector2> getBallPositions() {
        return ballPositions;
    }

    public IBot getBot() {
        return bot;
    }

    public BallVelocityInput getBallVelocityInput() {
        return ballVelocityInput;
    }

    /**
     * @return true, if the ball has stopped and the input window should open
     */
    public boolean isSimulationFinished() {
        boolean ballStopped = ballPositions.size() == 0;
        boolean notWaitingForBot = (bot == null || botThread == null) || (bot != null && !botThread.isAlive());
        boolean ballHasBeenPushed = shotForce == null;
        return ballHasBeenPushed && notWaitingForBot && ballStopped;
    }
    // endregion

    // region Mutator methods
    public void setShotForce(Vector2 shotForce) {
        this.shotForce = shotForce;
    }

    public void setManualInputType(BallVelocityInput ballInput) {
        ballVelocityInput = ballInput;
    }

    public void setBot(IBot bot) {
        this.bot = bot;
        if (bot != null) {
            drawArrow = false;
        }
    }
    // endregion
}
