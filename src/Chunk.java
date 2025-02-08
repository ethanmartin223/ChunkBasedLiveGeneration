import javax.swing.*;
import java.awt.*;

public class Chunk {

    public static Image WATER_IMAGE = new ImageIcon("buildAssets/0.png").getImage();
    public static Image GRASS_IMAGE = new ImageIcon("buildAssets/1.png").getImage();
    public static Image SAND_IMAGE = new ImageIcon("buildAssets/2.png").getImage();
    public static Image STONE_IMAGE = new ImageIcon("buildAssets/3.png").getImage();

    public int xWorldLocation;
    public int yWorldLocation;

    private double[][] dataLayer;
    private Image[][] renderLayer;

    NoiseGenerator perlin;

    public Chunk(int x, int y, NoiseGenerator perlin) {
        dataLayer = new double[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        renderLayer = new Image[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        xWorldLocation = x;
        yWorldLocation = y;
        this.perlin = perlin;
        generateDataLayer();
    }

    public void generateDataLayer() {
        for (int y=0; y<dataLayer.length; y++) {
            for (int x = 0; x < dataLayer.length; x++) {
                dataLayer[y][x] =  perlin.noise((double) (xWorldLocation * ChunkGenerator.CHUNK_SIZE + x),
                        (double) (yWorldLocation * ChunkGenerator.CHUNK_SIZE + y));
            }
        }
    }

    public void randomizeDataLayer() {
        for (int y=0; y<dataLayer.length; y++) {
            for (int x = 0; x < dataLayer.length; x++) {
                dataLayer[y][x] = (int) (Math.random() + .5);
            }
        }
    }

    @Override
    public String toString() {
        return "Chunk(x="+xWorldLocation+",y="+yWorldLocation+")";
    }

    public double[][] getDataLayer() {
        return dataLayer;
    }

    private void updateSpritesInRenderLayer() {
        for (int y=0; y<dataLayer.length; y++) {
            for (int x = 0; x < dataLayer.length; x++) {
                if (dataLayer[y][x] < 1) renderLayer[y][x] = STONE_IMAGE;
                if (dataLayer[y][x] < .5) renderLayer[y][x] = GRASS_IMAGE;
                if (dataLayer[y][x] < .09) renderLayer[y][x] = SAND_IMAGE;
                if (dataLayer[y][x] < .005) renderLayer[y][x] = WATER_IMAGE;
            }
        }
    }

    public Image[][] getRenderLayer() {
        updateSpritesInRenderLayer();
        return renderLayer;
    }

}
