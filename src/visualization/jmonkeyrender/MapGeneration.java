package visualization.jmonkeyrender;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.water.SimpleWaterProcessor;
import datastorage.Terrain;

public class MapGeneration {
    private final Renderer renderer;
    private final Terrain terrain;

    private final AssetManager assetManager;
    private final Node mainScene = new Node("Water");

    private final float terScale;
    private final float normalFactor;

    public MapGeneration(Renderer renderer) {
        this.renderer = renderer;
        this.terrain = renderer.getTerrain();

        this.terScale = renderer.getTerScale();
        this.normalFactor = renderer.getNormalFactor();

        this.assetManager = renderer.getAssetManager();
    }

    /**
     * Initializes area terrain based on the function given in input file
     * with textures of grass as well as sand if it is specified
     */
    private void initTerrain(String texPath){
        //Creating heightmap representation of the terrain
        AlphaMapGenerator.generateAlphaMap(terrain);

        //Setting terrain using heightmap
        TerrainQuad terrainQuad = new TerrainQuad("Course", 128, terrain.getVERTECES_PER_SIDE(), terrain.reversedHeightmap);

        //Grass Texture
        Material matTerrain = new Material(assetManager,"Common/MatDefs/Terrain/Terrain.j3md");
        matTerrain.setTexture("Alpha", renderer.getAssetManager().loadTexture(
                "Terrain/alpha.png"));
        Texture grass = assetManager.loadTexture(texPath);
        grass.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("Tex1", grass);
        matTerrain.setFloat("Tex1Scale", 64f);

        //Sand Texture
        Texture sand = assetManager.loadTexture(
                "Terrain/sand.jpeg");
        sand.setWrap(Texture.WrapMode.Repeat);
        matTerrain.setTexture("Tex2", sand);
        matTerrain.setFloat("Tex2Scale", 32f);

        //Lightens the terrain
        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.White.mult(5));
        renderer.getRootNode().addLight(amb);

        terrainQuad.setMaterial(matTerrain);
        terrainQuad.scale(1, terScale, 1);
        renderer.getRootNode().attachChild(terrainQuad);

        renderer.setTerrainQuad(terrainQuad);
    }

    /**
     * Creates a sky background as cube shape from 6 distinct images
     * @param path specification to load different background for different maps
     */
    private void initSky(String path){
        Texture westTex = assetManager.loadTexture(path+"/West.bmp");
        Texture eastTex = assetManager.loadTexture(path+"/East.bmp");
        Texture northTex = assetManager.loadTexture(path + "/North.bmp");
        Texture southTex = assetManager.loadTexture(path + "/South.bmp");
        Texture upTex = assetManager.loadTexture(path + "/Top.bmp");
        Texture downTex = assetManager.loadTexture(path + "/Bottom.bmp");

        mainScene.attachChild(SkyFactory.createSky(assetManager, westTex, eastTex, northTex, southTex, upTex, downTex));
        renderer.getRootNode().attachChild(mainScene);
    }

    /**
     * Spawns water simulation around the terrain
     */
    private void initWater(){
        //Spawn water only if the terrain has points<0
        if(this.terrain.minScaledVal<normalFactor/2) {
            //Creates new water object reflection
            SimpleWaterProcessor waterProcessor = new SimpleWaterProcessor(assetManager);
            waterProcessor.setLightPosition(new Vector3f(0.55f, -0.82f, 0.15f));
            waterProcessor.setReflectionScene(mainScene);

            //Setting the wave size
            Vector3f waterLocation = new Vector3f(0, 0, 0);
            waterProcessor.setPlane(new Plane(Vector3f.UNIT_Y, waterLocation.dot(Vector3f.UNIT_Y)));
            renderer.getViewPort().addProcessor(waterProcessor);

            //Creating the box of water'
            Quad waveSize = new Quad(terrain.getVERTECES_PER_SIDE()-1 + 200, terrain.getVERTECES_PER_SIDE()-1 + 200);
            Geometry water = new Geometry("water", waveSize);
            water.setShadowMode(RenderQueue.ShadowMode.Receive);
            water.setMaterial(waterProcessor.getMaterial());

            //Setting location to be around the terrain
            water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
            water.setLocalTranslation((float)-(terrain.getVERTECES_PER_SIDE()-1)/2-100, this.terrain.minScaledVal * terScale, (float)(terrain.getVERTECES_PER_SIDE()-1)/2+100);
            water.move(0, (normalFactor/2-this.terrain.minScaledVal) * terScale, 0);
            //Attaching water object to the scene
            renderer.getRootNode().attachChild(water);

        }
    }

    public void initMap(String terrainTexture){
        initTerrain(terrainTexture);
        initWater();
        initSky("Sky/BoxPieces");
    }
}
