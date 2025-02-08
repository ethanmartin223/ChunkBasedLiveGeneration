package buildTools;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class SpriteSlicerTool {
    final BufferedImage source;

    public SpriteSlicerTool() {

        try {
            source = ImageIO.read(new File("assets/tileset.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int spriteSize = 16;//px

        int spritesXCount = source.getWidth()/spriteSize;
        int spritesYCount = source.getHeight()/spriteSize;

        File buildAssetsDir = new File("buildassets/");
        for(File file: buildAssetsDir.listFiles())
            if (!file.isDirectory())
                file.delete();

        try {
            ImageIO.write(new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB), "png", new File("buildAssets/0.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int id = 1;
        for(int y = 0; y<spritesYCount;y +=1){
            for(int x = 0; x<spritesXCount;x +=1) {
                try {
                    ImageIO.write(source.getSubimage(x*spriteSize, y*spriteSize, spriteSize, spriteSize),
                            "png", new File("buildAssets/" + (id) + ".png"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                id++;
            }
        }
    }

    public static void main(String[] args) {
        new SpriteSlicerTool();
    }
}
