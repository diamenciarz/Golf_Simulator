package gui.shotinput;

import visualization.IInput;

public abstract class BallVelocityInput {
    protected IInput game;

    public BallVelocityInput(IInput game){this.game = game;}

    public abstract void readyForNextInput();

    public abstract void hideInputWindow();

    public abstract void stopListening();
}
