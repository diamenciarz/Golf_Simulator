package bot;

import datastorage.Ball;
import utility.math.Vector2;

import java.util.Random;

public class ShotNoiseGenerator {
    public Vector2 addNoiseToShot(Vector2 shot, double maxAmount) {
        Random random = new Random();

        Vector2 newShot = shot.copy();

        newShot.translate(new Vector2(
                (random.nextDouble()*2-1)*maxAmount,
                (random.nextDouble()*2-1)*maxAmount
        ));

        if (newShot.length() > Ball.maxSpeed) {
            newShot.normalize().scale(Ball.maxSpeed);
        }

        return newShot;
    }
}
