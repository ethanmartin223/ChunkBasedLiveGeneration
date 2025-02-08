package buildTools;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;

public class SetPieceGeneratorTool extends JFrame {

    public final static int CHUNK_SIZE = 8;

    private JPanel tilesContainerSidebar;
    private JPanel toolsContainerSidebar;
    private JPanel mainChunkEditorArea;

    private ArrayList<JButton> sidebarButtons;

    private ImageIcon currentPaintingTile;
    private int currentPaintingTileIndex;

    private int[][] currentOutputData;
    private boolean dragging;

    public SetPieceGeneratorTool() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setTitle("Set Piece Generator Tool");
        setLocationRelativeTo(null);

        currentOutputData = new int[CHUNK_SIZE][CHUNK_SIZE];

        File buildAssetsDir = new File("buildassets/");
        sidebarButtons = new ArrayList<>();

        tilesContainerSidebar = new JPanel();
        int columns = 3;
        tilesContainerSidebar.setLayout(new GridLayout(0, columns));

        currentPaintingTileIndex = 0;
        currentPaintingTile = null;

        JScrollPane scrollPane = new JScrollPane(tilesContainerSidebar);
        scrollPane.setPreferredSize(new Dimension(200, 1000));
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        add(scrollPane, BorderLayout.EAST);

        System.out.print("Slicing tileset in assets to buildAssets...");
        new SpriteSlicerTool();
        System.out.println(" Done");

        System.out.print("Setting Up Room creator tool...");
        File[] files = buildAssetsDir.listFiles();
        for (int fileId = 0; fileId < files.length; fileId++) {
            JButton temp = new JButton();
            temp.setPreferredSize(new Dimension(40, 80));
            temp.setFocusPainted(false);
            temp.setContentAreaFilled(false);
            temp.setMargin(new Insets(0, 0, 0, 0));
            temp.setIcon(getScaledImage(new ImageIcon("buildAssets/"+fileId+".png"), 55, 55));

            temp.addActionListener(e -> {
                currentPaintingTile = (ImageIcon) temp.getIcon();
                currentPaintingTileIndex = sidebarButtons.indexOf(temp);
            });

            sidebarButtons.add(temp);
            tilesContainerSidebar.add(temp);
        }

        mainChunkEditorArea = new JPanel();
        mainChunkEditorArea.setLayout(new GridLayout(CHUNK_SIZE, CHUNK_SIZE));
        mainChunkEditorArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainChunkEditorArea, BorderLayout.CENTER);

        for (int y = 0; y < CHUNK_SIZE; y++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                JButton temp = new JButton();
                temp.setFocusPainted(false);
                temp.setContentAreaFilled(false);
                temp.setMargin(new Insets(0, 0, 0, 0));

                int finalY = y;
                int finalX = x;
                temp.addMouseListener(new MouseListener() {

                    @Override
                    public void mouseClicked(MouseEvent e) {}
                    @Override
                    public void mousePressed(MouseEvent e) {
                        dragging = true;
                        temp.setIcon(getScaledImage(currentPaintingTile, mainChunkEditorArea.getWidth() / CHUNK_SIZE,
                                mainChunkEditorArea.getHeight() / CHUNK_SIZE));
                        currentOutputData[finalY][finalX] = currentPaintingTileIndex;
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        dragging = false;
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (dragging && currentPaintingTile!= null) {
                            temp.setIcon(getScaledImage(currentPaintingTile, mainChunkEditorArea.getWidth() / CHUNK_SIZE,
                                    mainChunkEditorArea.getHeight() / CHUNK_SIZE));
                            currentOutputData[finalY][finalX] = currentPaintingTileIndex;
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {}
                });
                mainChunkEditorArea.add(temp);
            }
        }
        System.out.println(" Done\n");

        toolsContainerSidebar = new JPanel();
        toolsContainerSidebar.setPreferredSize(new Dimension(200, 1000));
        toolsContainerSidebar.setLayout(new BoxLayout(toolsContainerSidebar, BoxLayout.Y_AXIS));
        add(toolsContainerSidebar, BorderLayout.WEST);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            addToFile("assets/rooms.data", Arrays.deepToString(currentOutputData).replace("[", "").replace("]", "").replace(", ", " "));

            System.out.println("Room saved to assets/rooms.data");
        });
        toolsContainerSidebar.add(saveButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    currentOutputData[y][x] = 0;
                    JButton temp = (JButton) mainChunkEditorArea.getComponent(y * CHUNK_SIZE + x);
                    temp.setIcon(null);
                }
            }
        });
        toolsContainerSidebar.add(clearButton);

        setVisible(true);
    }

    private ImageIcon getScaledImage(ImageIcon srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(srcImg.getImage(), 0, 0, w, h, null);
        g2.dispose();

        return new ImageIcon(resizedImg);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SetPieceGeneratorTool::new); // Ensures thread safety for GUI
    }

    public void addToFile(String fileName, String data) {
        try (FileWriter fileWriter = new FileWriter(fileName, true);
               BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(data);
            bufferedWriter.newLine();
        } catch (IOException e) {
            System.err.println("An error occurred while appending to the file: " + e.getMessage());
        }
    }
}
