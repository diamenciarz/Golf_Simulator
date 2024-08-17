package visualization.gameengine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Input implements KeyListener{
    public static final int A = KeyEvent.VK_A,
            B = KeyEvent.VK_B,
            C = KeyEvent.VK_C,
            D = KeyEvent.VK_D,
            E = KeyEvent.VK_E,
            F = KeyEvent.VK_F,
            G = KeyEvent.VK_G,
            H = KeyEvent.VK_H,
            I = KeyEvent.VK_I,
            J = KeyEvent.VK_J,
            K = KeyEvent.VK_K,
            L = KeyEvent.VK_L,
            M = KeyEvent.VK_M,
            N = KeyEvent.VK_N,
            O = KeyEvent.VK_O,
            P = KeyEvent.VK_P,
            Q = KeyEvent.VK_Q,
            R = KeyEvent.VK_R,
            S = KeyEvent.VK_S,
            T = KeyEvent.VK_T,
            U = KeyEvent.VK_U,
            V = KeyEvent.VK_V,
            W = KeyEvent.VK_W,
            X = KeyEvent.VK_X,
            Y = KeyEvent.VK_Y,
            Z = KeyEvent.VK_Z,
            ZERO = KeyEvent.VK_0,
            ONE = KeyEvent.VK_1,
            TWO = KeyEvent.VK_2,
            THREE = KeyEvent.VK_3,
            FOUR = KeyEvent.VK_4,
            FIVE = KeyEvent.VK_5,
            SIX = KeyEvent.VK_6,
            SEVEN = KeyEvent.VK_7,
            EIGHT = KeyEvent.VK_8,
            NINE = KeyEvent.VK_9,
            SPACE = KeyEvent.VK_SPACE,
            SHIFT = KeyEvent.VK_SHIFT,
            ENTER = KeyEvent.VK_ENTER,
            LEFT_MOUSE = MouseEvent.BUTTON1,
            RIGHT_MOUSE = MouseEvent.BUTTON2;

    private ArrayList<Integer> keysPressed = new ArrayList<Integer>();
    private ArrayList<Integer> keysDown = new ArrayList<Integer>();
    private ArrayList<Integer> keysWerePressed = new ArrayList<Integer>();

    /**
     * Stores a pressed key into the list of pressed and held down keys
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (!keysDown.contains(key)) {
            keysPressed.add(key);
            keysWerePressed.add(key);
            keysDown.add(key);
        }
    }

    /**
     * Removes a released key from the pressed and held down keys list
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (keysDown.contains(key)) {
            keysDown.remove((Integer) key);
        }
        if (keysPressed.contains(key)) {
            keysPressed.remove((Integer) key);
        }
    }

    

    public void mouseReleased(MouseEvent e) {
        int key = e.getID();
        if (keysDown.contains(key)) {
            keysDown.remove((Integer) key);
        }
        if (keysPressed.contains(key)) {
            keysPressed.remove((Integer) key);
        }
    }

    /**
     * Checks if a key was pressed
     * 
     * @param key The ID of the key to check
     * @return true if the key was pressed and false otherwise
     */
    public boolean isPressed(int key) {
        return keysPressed.contains(key);
    }

    /**
     * Checks if a key is being held down
     * 
     * @param key The ID of the key to check
     * @return true if the key is being held down and false otherwise
     */
    public boolean isDown(int key) {
        return keysDown.contains(key);
    }

    /**
     * Checks if a key was clicked
     * 
     * @param key The ID of the key to check
     * @return true if the key was clicked and false otherwise
     */
    public boolean wasClicked(int key) {
        return keysWerePressed.contains(key);
    }

    /**
     * Removes all the pressed keys.
     * This code is executed in the Game.step() method
     */
    public void removeFromPressed() {
        for (int key : keysWerePressed) {
            if (keysPressed.contains(key)) {
                keysPressed.remove((Integer) key);
            }
        }
        keysWerePressed = new ArrayList<Integer>();
    }

    // region Unused
    public void keyTyped(KeyEvent e) {
    }
    // endregion
}
