import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WorldRenderer extends JPanel {

    public static int worldRenderDistance = 2;
    public static int spriteSizeOffset = 16;

    ChunkGenerator worldChunkGenerator;
    int chunkThatPlayerIsCurrentlyInX;
    int chunkThatPlayerIsCurrentlyInY;

    private final boolean renderChunkBorder = false;

    long timeStamp;
    long deltaTime;
    int xPress, yPress;
    double cameraXLocation, cameraYLocation;
    double lastReleasedPositionY, lastReleasedPositionX;


    private int zoomLevel = 1;
    private static final int ZOOM_FACTOR = 1;

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


        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) {
                    zoomLevel += ZOOM_FACTOR;
                } else {
                    zoomLevel = Math.max(zoomLevel - ZOOM_FACTOR, 1);
                }
                repaint();
            }
        });

    }

    private void renderChunk(int posX, int posY, Graphics2D g2d) {
        Image[][] sprites = worldChunkGenerator.grabChunk(posX, posY).getRenderLayer();

        int offsetX = (int) (256 + cameraXLocation + posX * spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel);
        int offsetY = (int) (256 + cameraYLocation + posY * spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel);

        for (int y = 0; y < sprites.length; y++) {
            for (int x = 0; x < sprites[y].length; x++) {
                int scaledWidth = spriteSizeOffset * zoomLevel;
                int scaledHeight = spriteSizeOffset * zoomLevel;


                g2d.drawImage(sprites[y][x],
                        (int) (offsetX + x * scaledWidth),
                        (int) (offsetY + y * scaledHeight),
                        scaledWidth,
                        scaledHeight,
                        null);
            }
        }

        g2d.setColor(Color.red);
        if (renderChunkBorder) {
            g2d.drawRect(offsetX, offsetY,
                    (int) (spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel),
                    (int) (spriteSizeOffset * ChunkGenerator.CHUNK_SIZE * zoomLevel));
            g2d.drawString("(" + posX + ", " + posY + ")",
                    offsetX,
                    offsetY - 10);
        }
    }

    public void renderALlChunksInRadiusOf(int posX, int posY, int chunkRadius, Graphics2D g2d) {
        for (int y = (posY - chunkRadius); y < posY + (chunkRadius + 1); y++) {
            for (int x = (posX - chunkRadius); x < posX + (chunkRadius + 1); x++) {
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

        chunkThatPlayerIsCurrentlyInX = -(int) (cameraXLocation / (256 * zoomLevel)) + (cameraXLocation < 0 ? 1 : 0);
        chunkThatPlayerIsCurrentlyInY = -(int) (cameraYLocation / (256 * zoomLevel)) + (cameraYLocation < 0 ? 1 : 0);

    }
}
