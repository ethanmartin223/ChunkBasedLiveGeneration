import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Chunk {

    public static Image GROUND_IMAGE = new ImageIcon("assets/Ground.png").getImage();

    public int xWorldLocation;
    public int yWorldLocation;

    private int[][] dataLayer;
    private Image[][] renderLayer;

    public Chunk(int x, int y) {
        dataLayer = new int[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        renderLayer = new Image[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        xWorldLocation = x;
        yWorldLocation = y;

        if (x%4!=0 || y%4!=0) randomizeDataLayer();
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

    public int[][] getDataLayer() {
        return dataLayer;
    }

    private void updateSpritesInRenderLayer() {
        for (int y=0; y<dataLayer.length; y++) {
            for (int x = 0; x < dataLayer.length; x++) {
                if (dataLayer[y][x] == 1) {
                    renderLayer[y][x] = GROUND_IMAGE;
                } else renderLayer[y][x] = null;
            }
        }
    }

    public Image[][] getRenderLayer() {
        updateSpritesInRenderLayer();
        return renderLayer;
    }

}
