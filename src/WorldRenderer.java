import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WorldRenderer extends JPanel {

    public static int worldRenderDistance = 1;
    public static int spriteSizeOffset = 16;

    ChunkGenerator worldChunkGenerator;
    int chunkThatPlayerIsCurrentlyInX;
    int chunkThatPlayerIsCurrentlyInY;

    private boolean renderChunkBorder = true;


    long timeStamp;
    long deltaTime;
    int xPress, yPress;
    double cameraXLocation, cameraYLocation;
    double lastReleasedPositionY, lastReleasedPositionX;


    public WorldRenderer(ChunkGenerator cgn) {
        worldChunkGenerator = cgn;
        chunkThatPlayerIsCurrentlyInX = 0;
        chunkThatPlayerIsCurrentlyInY = 0;

        timeStamp = System.currentTimeMillis();
        deltaTime = System.currentTimeMillis();
        repaint();

        cameraXLocation = 350;
        cameraYLocation = 350;
        lastReleasedPositionX = 350;
        lastReleasedPositionY = 350;


        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                xPress = e.getX();
                yPress = e.getY();
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                lastReleasedPositionX = cameraXLocation;
                lastReleasedPositionY = cameraYLocation;
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int xDragDelta = e.getX() - xPress;
                int yDragDelta = e.getY() - yPress;
                cameraXLocation = lastReleasedPositionX + xDragDelta;
                cameraYLocation = lastReleasedPositionY + yDragDelta;
                e.getComponent().repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {}
        });

    }

    private void renderChunk(int posX, int posY, Graphics2D g2d) {
        Image[][] sprites = worldChunkGenerator.grabChunk(posX, posY).getRenderLayer();

        if (posX%4==0 && posY%4==0) {
            g2d.drawString(""+posX+", "+posY,
                    256+(int) cameraXLocation +posX*spriteSizeOffset*ChunkGenerator.CHUNK_SIZE +ChunkGenerator.CHUNK_SIZE/2*spriteSizeOffset,
                    256+(int) cameraYLocation +posY*spriteSizeOffset*ChunkGenerator.CHUNK_SIZE +ChunkGenerator.CHUNK_SIZE/2*spriteSizeOffset);
        }


        for (int y=0; y<sprites.length; y++) {
            for (int x=0; x<sprites.length; x++) {
                g2d.scale(1, 1);
                g2d.drawImage(sprites[y][x],
                        256+(int) cameraXLocation +posX*spriteSizeOffset*ChunkGenerator.CHUNK_SIZE +x*spriteSizeOffset,
                        256+(int) cameraYLocation +posY*spriteSizeOffset*ChunkGenerator.CHUNK_SIZE +y*spriteSizeOffset,
                        null);
                //System.out.println("rendered sprite ("+x+","+y+") of Chunk(x="+posX+",y="+posY+")");
            }
        }

        g2d.setColor(Color.red);
        if (renderChunkBorder) {
            g2d.drawRect(256+(int) cameraXLocation +posX*spriteSizeOffset*ChunkGenerator.CHUNK_SIZE,
                    256+(int) cameraYLocation +posY*spriteSizeOffset*ChunkGenerator.CHUNK_SIZE,
                    spriteSizeOffset*ChunkGenerator.CHUNK_SIZE,
                    spriteSizeOffset*ChunkGenerator.CHUNK_SIZE);
        }
    }


    //render all chunks surrounding the chunk (posX, posY), in a radius of chunkRadius
    public void renderALlChunksInRadiusOf(int posX, int posY, int chunkRadius, Graphics2D g2d) {

        for (int y=(posY-(chunkRadius)); y<posY+(chunkRadius+1); y++) {
            for (int x=(posX-(chunkRadius)); x<posX+(chunkRadius+1); x++) {
                renderChunk(x, y, g2d);
            }
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        renderALlChunksInRadiusOf(chunkThatPlayerIsCurrentlyInX, chunkThatPlayerIsCurrentlyInY, worldRenderDistance, g2d);
        g2d.dispose();


        chunkThatPlayerIsCurrentlyInX = -(int) (cameraXLocation / 256)  +(cameraXLocation<0?1:0);
        chunkThatPlayerIsCurrentlyInY = -(int) (cameraYLocation / 256)  +(cameraYLocation<0?1:0);

        System.out.println("\n\n\n\ncameraXLocation: "+cameraXLocation);
        System.out.println("cameraYLocation: "+cameraYLocation);
        System.out.println("worldRenderingXLocation: "+ chunkThatPlayerIsCurrentlyInX);
        System.out.println("worldRenderingYLocation: "+ chunkThatPlayerIsCurrentlyInY);
        worldChunkGenerator.listChunksThatExist();
        System.out.println(worldChunkGenerator.allChunksList.size()+ " Chunks Currently loaded into mem");

    }

}

