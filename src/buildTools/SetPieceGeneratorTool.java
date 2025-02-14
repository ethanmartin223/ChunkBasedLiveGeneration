package buildTools;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.*;

public class SetPieceGeneratorTool extends JFrame {

    public final static int CHUNK_SIZE = 8;

    private JLabel roomIDLabel;
    private JPanel tilesContainerSidebar;
    private JPanel toolsContainerSidebar;
    private JPanel mainChunkEditorArea;
    private JTextField roomIndexField;

    private ArrayList<JButton> sidebarButtons;

    private ImageIcon currentPaintingTile;
    private int currentPaintingTileIndex;

    private int[][] currentOutputData;
    private boolean dragging;
    private int currentRoomIndex;
    private ArrayList<int[]> savedRooms;

    public static int[] walls = new int[] {0, 2, 3, 8, 9, 16, 17, 24, 25, 32, 33, 40, 41};

    public static boolean isWall(int[] data, int index) {
        int tileData = data[index];
        for (int i = 0; i < walls.length; i++) {
            if (tileData+1 == walls[i]) return true;
        }
        return false;
    }

    private static String checkWallLocations(int[] roomData) {
        String identifier = "";
        if (!isWall(roomData, 2) && !isWall(roomData, 3) && !isWall(roomData, 4) && !isWall(roomData, 5)) identifier += "N";
        if (!isWall(roomData, 23) && !isWall(roomData, 31) && !isWall(roomData, 39) && !isWall(roomData, 47)) identifier += "E";
        if (!isWall(roomData, 58) && !isWall(roomData, 59) && !isWall(roomData, 60) && !isWall(roomData, 61)) identifier += "S";
        if (!isWall(roomData, 16) && !isWall(roomData, 24) && !isWall(roomData, 32) && !isWall(roomData, 40)) identifier += "W";
        return identifier;
    }

    public SetPieceGeneratorTool() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setTitle("Set Piece Generator Tool");
        setLocationRelativeTo(null);

        currentOutputData = new int[CHUNK_SIZE][CHUNK_SIZE];
        savedRooms = new ArrayList<>();

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
            temp.setIcon(getScaledImage(new ImageIcon("buildAssets/" + fileId + ".png"), 55, 55));

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
                        if (dragging && currentPaintingTile != null) {
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

        roomIDLabel = new JLabel();
        roomIDLabel.setText(checkWallLocations(flatten(currentOutputData)));
        toolsContainerSidebar.add(roomIDLabel);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            if (currentRoomIndex < savedRooms.size()) {
                savedRooms.set(currentRoomIndex, Arrays.stream(currentOutputData)
                        .flatMapToInt(Arrays::stream)
                        .toArray());
                saveAllRoomsToFile("assets/rooms.data");
                System.out.println("Room " + currentRoomIndex + " saved to assets/rooms.data");
            } else {
                addToFile("assets/rooms.data", Arrays.deepToString(currentOutputData)
                        .replace("[", "").replace("]", "").replace(", ", " "));
                loadRoomsFromFile("assets/rooms.data");
                System.out.println("New room saved to assets/rooms.data");
            }
            roomIDLabel.setText(checkWallLocations(flatten(currentOutputData)));

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
            roomIDLabel.setText(checkWallLocations(flatten(currentOutputData)));
        });
        toolsContainerSidebar.add(clearButton);

        JPanel navigationPanel = new JPanel();
        JButton previousButton = new JButton("<");
        previousButton.addActionListener(e -> loadPreviousRoom());
        JButton nextButton = new JButton(">");
        nextButton.addActionListener(e -> loadNextRoom());

        roomIndexField = new JTextField(5);
        roomIndexField.addActionListener(e -> {
            try {
                int index = Integer.parseInt(roomIndexField.getText());
                loadRoom(index);
            } catch (NumberFormatException ex) {}
        });

        navigationPanel.add(previousButton);
        navigationPanel.add(roomIndexField);
        navigationPanel.add(nextButton);
        toolsContainerSidebar.add(navigationPanel);

        loadRoomsFromFile("assets/rooms.data");
        setVisible(true);
    }

    private int[] flatten(int[][] currentOutputData) {
        int[] flatData = new int[CHUNK_SIZE * CHUNK_SIZE];
        int index = 0;
        for (int[] row : currentOutputData) {
            for (int value : row) {
                flatData[index++] = value;
            }
        }
        return flatData;
    }

    private void saveAllRoomsToFile(String fileName) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName))) {
            for (int[] room : savedRooms) {
                String roomData = Arrays.toString(room).replace("[", "").replace("]", "").replace(",", "");
                bufferedWriter.write(roomData);
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            System.err.println("An error occurred while saving rooms: " + e.getMessage());
        }
    }

    private void loadRoomsFromFile(String fileName) {
        savedRooms.clear();
        try (Scanner scanner = new Scanner(new FileReader(fileName))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    int[] roomData = Arrays.stream(line.split(" "))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    savedRooms.add(roomData);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rooms: " + e.getMessage());
        }
        currentRoomIndex = savedRooms.size();
        if (!savedRooms.isEmpty()) {
            loadRoom(currentRoomIndex);
        }
        roomIndexField.setText(String.valueOf(currentRoomIndex));

    }

    private void loadRoom(int index) {
        if (index >= 0 && index < savedRooms.size()) {
            currentOutputData = new int[CHUNK_SIZE][CHUNK_SIZE];
            int[] roomData = savedRooms.get(index);
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    currentOutputData[y][x] = roomData[y * CHUNK_SIZE + x];
                    JButton temp = (JButton) mainChunkEditorArea.getComponent(y * CHUNK_SIZE + x);
                    if (currentOutputData[y][x] >= 0 && currentOutputData[y][x] < sidebarButtons.size()) {
                        temp.setIcon(getScaledImage((ImageIcon) sidebarButtons.get(currentOutputData[y][x]).getIcon(),
                                mainChunkEditorArea.getWidth() / CHUNK_SIZE,
                                mainChunkEditorArea.getHeight() / CHUNK_SIZE));
                    } else {
                        temp.setIcon(null);
                    }
                }
            }
            currentRoomIndex = index;
        } else {
            currentOutputData = new int[CHUNK_SIZE][CHUNK_SIZE];
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    currentOutputData[y][x] = 0;
                    JButton temp = (JButton) mainChunkEditorArea.getComponent(y * CHUNK_SIZE + x);
                    temp.setIcon(null);
                }
            }
            currentRoomIndex = index;
        }
        roomIDLabel.setText(checkWallLocations(flatten(currentOutputData)));
    }

    private void loadPreviousRoom() {
        if (currentRoomIndex > 0) {
            loadRoom(--currentRoomIndex);
            roomIndexField.setText(currentRoomIndex+"");
        }
    }

    private void loadNextRoom() {
        loadRoom(++currentRoomIndex);
        roomIndexField.setText(currentRoomIndex+"");
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
