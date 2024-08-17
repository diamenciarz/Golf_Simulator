package visualization.jmonkeyrender;

import datastorage.Terrain;
import datastorage.Zone;

import javax.imageio.ImageIO;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class AlphaMapGenerator {

    public static void generateAlphaMap(Terrain terrain) {
        float DEFAULT_SIZE = (float)terrain.getVERTECES_PER_SIDE()-1;

        final BufferedImage image = new BufferedImage ( (int)DEFAULT_SIZE, (int)DEFAULT_SIZE, BufferedImage.TYPE_INT_ARGB );
        final Graphics2D graphics2D = image.createGraphics ();

        //This sets everything to grass
        graphics2D.setPaint ( Color.RED );
        graphics2D.fillRect ( 0,0,(int)DEFAULT_SIZE,(int)DEFAULT_SIZE );

        //This sets any sandpit that is required
        for(Zone i: terrain.zones){
            graphics2D.setColor(Color.GREEN);
            graphics2D.fillRect((int) Math.round((i.bottomLeftCorner.x*10.24)+(DEFAULT_SIZE/2)),(int)Math.round((i.bottomLeftCorner.y*10.24)+DEFAULT_SIZE/2),
                    (int)((i.topRightCorner.x-i.bottomLeftCorner.x)*10.24),(int)((i.topRightCorner.y-i.bottomLeftCorner.y)*10.24));
        }

        graphics2D.dispose ();
        try {
            ImageIO.write(image, "png", new File(System.getProperty("user.dir") + "/src/main/resources/Terrain/alpha.png"));
            ImageIO.write(image, "png", new File(System.getProperty("user.dir") + "/target/classes/Terrain/alpha.png"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}