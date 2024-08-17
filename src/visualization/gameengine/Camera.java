package visualization.gameengine;

public class Camera {
    public Camera(double width, double height) {
        WIDTH = width;
        HEIGHT = height;
    }

    public double WIDTH;
    public double HEIGHT; // In units
    public double xPos, yPos; // In units
}
