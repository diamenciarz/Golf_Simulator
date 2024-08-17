package gui.shotinput;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

import visualization.IInput;
import utility.math.Vector2;

public class InputFileReader extends BallVelocityInput {

    public InputFileReader(IInput game){
        super(game);
    }
    
    static String fileName = "Data";
    static Scanner fileScanner;
    static Queue<Vector2> commandQueue;

    private static boolean hasUpdatedQueue = false;

    public static void setReadfileName(String name) {
        fileName = name;
        hasUpdatedQueue = false;
    }

    @Override
    public void readyForNextInput(){
        game.getUpdateLoop().setShotForce(getForce());
    }

    @Override
    public void hideInputWindow() {}

    private Vector2 getForce() {
        commandQueue = getShotQueue();
        if (hasUpdatedQueue) {
            return commandQueue.poll();
        }
        return null;
    }

    private static Queue<Vector2> getShotQueue() {
        if (!resetScanner()) {
            return null;
        }

        hasUpdatedQueue = true;
        return getData();
    }

    /**
     * Sets up scanner
     * 
     * @return true, if the scanner has been setup successfully
     */
    private static boolean resetScanner() {
        try {
            FileReader fileReader = new FileReader(fileName + ".txt");
            fileScanner = new Scanner(fileReader);
            return true;

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
            return false;
        }
    }

    private static Queue<Vector2> getData() {

        Queue<Vector2> movementQueue = new ArrayDeque<>();

        while (fileScanner.hasNextLine()) {

            String inputLine = fileScanner.nextLine();
            Vector2 vector = parseIntoVector(inputLine);

            if (vector.equals(Vector2.zeroVector())) {
                // Zero vector means that there was a problem with parsing. A zero vector is
                // useless anyway
                continue;
            }

            movementQueue.add(vector);
        }

        return movementQueue;
    }

    /**
     * The correct string should look like this:
     * [double],[double]
     * 
     * @param input
     * @return
     */
    private static Vector2 parseIntoVector(String input) {
        if (checkNull(input)) {
            return Vector2.zeroVector();
        }

        String[] splitSequence = input.split(",");

        if (checkIncorrectLength(splitSequence)) {
            return Vector2.zeroVector();
        }

        return tryParse(splitSequence);
    }

    // region Error checks
    private static boolean checkNull(String input) {
        if (input == null) {
            System.out.println("String was null - meaning, line was empty");
            return true;
        }
        return false;
    }

    private static boolean checkIncorrectLength(String[] splitSequence) {
        if (splitSequence.length != 2) {
            System.out.println("The string does not contain 2 numbers.");
            System.out.println("The correct input should look like this: [double],[double]");

            return true;
        }
        return false;
    }

    private static Vector2 tryParse(String[] splitSequence) {
        try {
            double x = Double.parseDouble(splitSequence[0]);
            double y = Double.parseDouble(splitSequence[1]);
            return new Vector2(x, y);

        } catch (Exception e) {
            System.out.println("The string does not contain a parsable dpuble.");
            System.out.println("The correct input should look like this: [double],[double]");
            return Vector2.zeroVector();

        }
    }
    // endregion
    // endregion

    @Override
    public void stopListening() {
        // Do nothing
    }
}
