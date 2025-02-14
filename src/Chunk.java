import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static buildTools.SetPieceGeneratorTool.CHUNK_SIZE;

public class Chunk {

    private ChunkGenerator parent;

    public static ArrayList<int[]> chunkDataPresets;
    static {
        chunkDataPresets = new ArrayList<>();
        try (Scanner scanner = new Scanner(new FileReader("assets/rooms.data"))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    int[] roomData = Arrays.stream(line.split(" "))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    chunkDataPresets.add(roomData);
                }
            }
        } catch (IOException e) {}
    }


    //    0  1  2  3  4  5  6  7
    //    8  9 10 11 12 13 14 15
    //    16 17 18 19 20 21 22 23
    //    24 25 26 27 28 29 30 31
    //    32 33 34 35 36 37 38 39
    //    40 41 42 43 44 45 46 47
    //    48 49 50 51 52 53 54 55
    //    56 57 58 59 60 61 62 63

    public static int[] walls = new int[] {0, 2, 3, 8, 9, 16, 17, 24, 25, 32, 33, 40, 41};
    public static Map<String, ArrayList<int[]>> identifierToDataPresets;
    public static Map<int[], String> dataToIdentifierPresets;

    static {
        identifierToDataPresets = new HashMap<>();
        dataToIdentifierPresets = new HashMap<>();

        for (int[] roomData : chunkDataPresets) {
            String identifier = checkWallLocations(roomData);
            if (identifierToDataPresets.containsKey(identifier)) {
                identifierToDataPresets.get(identifier).add(roomData);
            } else {
                identifierToDataPresets.put(identifier, new ArrayList<>());
                identifierToDataPresets.get(identifier).add(roomData);
            }
            dataToIdentifierPresets.put(roomData, identifier);
        }
        
    }

    private static String checkWallLocations(int[] roomData) {
        String identifier = "";
        if (!isWall(roomData, 2) && !isWall(roomData, 3) && !isWall(roomData, 4) && !isWall(roomData, 5)) identifier += "N";
        if (!isWall(roomData, 23) && !isWall(roomData, 31) && !isWall(roomData, 39) && !isWall(roomData, 47)) identifier += "E";
        if (!isWall(roomData, 58) && !isWall(roomData, 59) && !isWall(roomData, 60) && !isWall(roomData, 61)) identifier += "S";
        if (!isWall(roomData, 16) && !isWall(roomData, 24) && !isWall(roomData, 32) && !isWall(roomData, 40)) identifier += "W";
        return identifier;
    }

    public static boolean isWall(int[] data, int index) {
        int tileData = data[index];
        for (int i = 0; i < walls.length; i++) {
            if (tileData+1 == walls[i]) return true;
        }
        return false;
    }


    public static ArrayList<ImageIcon> sprites;
    static {
        sprites = new ArrayList<>();

        File buildAssetsDir = new File("buildAssets/");
        File[] files = buildAssetsDir.listFiles();
        for (int fileId = 0; fileId < files.length; fileId++) {
            sprites.add(new ImageIcon(buildAssetsDir.getAbsolutePath() + "/" + fileId + ".png"));
        }
    }



    public int xWorldLocation;
    public int yWorldLocation;

    private int[][] dataLayer;
    private Image[][] renderLayer;

    private String[][] subChunkIdentifiers;

    int subChunkSize = ChunkGenerator.CHUNK_SIZE / 2;

    public Chunk(int x, int y, ChunkGenerator chunkGenerator) {
        subChunkIdentifiers = new String[ChunkGenerator.CHUNK_SIZE / subChunkSize][ChunkGenerator.CHUNK_SIZE / subChunkSize];
        dataLayer = new int[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        renderLayer = new Image[ChunkGenerator.CHUNK_SIZE][ChunkGenerator.CHUNK_SIZE];
        xWorldLocation = x;
        yWorldLocation = y;
        parent = chunkGenerator;
        generateDataLayer();
    }

    public void generateDataLayer() {
        Random rand = new Random();

        if (rand.nextInt(100) < ChunkGenerator.DUMMY_CHUNK_CHANCE_PERCENT) {
            for (int x = 0; x < ChunkGenerator.CHUNK_SIZE; x++) {
                for (int y = 0; y < ChunkGenerator.CHUNK_SIZE; y++) {
                    dataLayer[x][y] = 1;
                }
            }
            return;
        }

        for (int chunkX = 0; chunkX < ChunkGenerator.CHUNK_SIZE; chunkX += subChunkSize) {
            for (int chunkY = 0; chunkY < ChunkGenerator.CHUNK_SIZE; chunkY += subChunkSize) {

                int[] chunkData = new int[64];
                if (chunkX==0 && chunkY==0 && !parent.firstChunkSpawned) {
                    chunkData = chunkDataPresets.get(rand.nextInt(chunkDataPresets.size()));
                    subChunkIdentifiers[chunkY][chunkX] = dataToIdentifierPresets.get(chunkData);
                } else {
                    String lookingForIdentifier = "";
                    int subChunkX = chunkX / subChunkSize;
                    int subChunkY = chunkY / subChunkSize;

                    //North border
                    if (subChunkY-1>-1 && subChunkIdentifiers[subChunkY-1][subChunkX]!= null
                            && subChunkIdentifiers[subChunkY-1][subChunkX].contains("S"))
                        lookingForIdentifier += "N";
                    else if (subChunkY-1==-1 && parent.hasChunkBeenGeneratedAt(xWorldLocation, yWorldLocation - 1)) {
                         Chunk externalChunk = parent.grabChunk(xWorldLocation, yWorldLocation - 1);
                         String chunkIdents = externalChunk.subChunkIdentifiers[ChunkGenerator.CHUNK_SIZE/subChunkSize-1-subChunkY][subChunkX];
                         if (chunkIdents.contains("S")) lookingForIdentifier += "N";
                    }
                    // if the chunk border is up against a chunk that doesn't exist yet, pick a random value using the percentage chance setting
                    else if (!parent.hasChunkBeenGeneratedAt(xWorldLocation, yWorldLocation - 1)) {
                        if (rand.nextInt(100)< ChunkGenerator.CROSS_CHUNK_BRANCHING_CHANCE_PERCENT) lookingForIdentifier += "N";
                    }

                    //East border
                    if (subChunkX+1<(ChunkGenerator.CHUNK_SIZE / subChunkSize) &&
                            subChunkIdentifiers[subChunkY][subChunkX+1] != null
                            && subChunkIdentifiers[subChunkY][subChunkX+1].contains("W"))
                        lookingForIdentifier += "E";
                    else if (subChunkX+1>=(ChunkGenerator.CHUNK_SIZE / subChunkSize) && parent.hasChunkBeenGeneratedAt(xWorldLocation+1, yWorldLocation)) {
                        Chunk externalChunk = parent.grabChunk(xWorldLocation+1, yWorldLocation);
                        String chunkIdents = externalChunk.subChunkIdentifiers[subChunkY][ChunkGenerator.CHUNK_SIZE/subChunkSize-1-subChunkX];
                        if (chunkIdents.contains("W")) lookingForIdentifier += "E";
                    }else if (!parent.hasChunkBeenGeneratedAt(xWorldLocation+1, yWorldLocation)) {
                        if (rand.nextInt(100)< ChunkGenerator.CROSS_CHUNK_BRANCHING_CHANCE_PERCENT) lookingForIdentifier += "E";
                    }


                    //South border
                    if (subChunkY+1<(ChunkGenerator.CHUNK_SIZE / subChunkSize) &&
                            subChunkIdentifiers[subChunkY+1][subChunkX]!= null
                            && subChunkIdentifiers[subChunkY+1][subChunkX].contains("N"))
                        lookingForIdentifier += "S";
                    else if (subChunkY+1>=(ChunkGenerator.CHUNK_SIZE / subChunkSize) && parent.hasChunkBeenGeneratedAt(xWorldLocation, yWorldLocation + 1)) {
                        Chunk externalChunk = parent.grabChunk(xWorldLocation, yWorldLocation + 1);
                        String chunkIdents = externalChunk.subChunkIdentifiers[ChunkGenerator.CHUNK_SIZE/subChunkSize-1-subChunkY][subChunkX];
                        if (chunkIdents.contains("N")) lookingForIdentifier += "S";
                    }else if (!parent.hasChunkBeenGeneratedAt(xWorldLocation, yWorldLocation + 1)) {
                        if (rand.nextInt(100)< ChunkGenerator.CROSS_CHUNK_BRANCHING_CHANCE_PERCENT) lookingForIdentifier += "S";
                    }


                    //West border
                    if (subChunkX-1>-1 && subChunkIdentifiers[subChunkY][subChunkX-1]!= null
                            && subChunkIdentifiers[subChunkY][subChunkX-1].contains("E"))
                        lookingForIdentifier += "W";
                    else if (subChunkX-1==-1 && parent.hasChunkBeenGeneratedAt(xWorldLocation-1, yWorldLocation)) {
                        Chunk externalChunk = parent.grabChunk(xWorldLocation-1, yWorldLocation);
                        String chunkIdents = externalChunk.subChunkIdentifiers[subChunkY][ChunkGenerator.CHUNK_SIZE/subChunkSize-1-subChunkX];
                        if (chunkIdents.contains("E")) lookingForIdentifier += "W";
                    }else if (!parent.hasChunkBeenGeneratedAt(xWorldLocation-1, yWorldLocation)) {
                        if (rand.nextInt(100)< ChunkGenerator.CROSS_CHUNK_BRANCHING_CHANCE_PERCENT) lookingForIdentifier += "W";
                    }


                    ArrayList<int[]> chunkDataChoices = identifierToDataPresets.get(lookingForIdentifier);
                    if (chunkDataChoices== null) System.out.println("ERROR NO ROOM WITH IDENTIFIERS COMBINATION: "+lookingForIdentifier);
                    chunkData = chunkDataChoices.get(rand.nextInt(chunkDataChoices.size()));
                    subChunkIdentifiers[subChunkY][subChunkX] = lookingForIdentifier;
                }
                for (int subChunkX = 0; subChunkX < subChunkSize; subChunkX++) {
                    for (int subChunkY = 0; subChunkY < subChunkSize; subChunkY++) {
                        int index = subChunkY * subChunkSize + subChunkX;
                        dataLayer[chunkY + subChunkY][chunkX + subChunkX] = chunkData[index];
                    }
                }
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

    public int[][] getDataLayer() {
        return dataLayer;
    }

    private void updateSpritesInRenderLayer() {
        for (int y=0; y<dataLayer.length; y++) {
            for (int x = 0; x < dataLayer.length; x++) {
                renderLayer[y][x] = sprites.get(dataLayer[y][x]).getImage();
            }
        }
    }

    public Image[][] getRenderLayer() {
        updateSpritesInRenderLayer();
        return renderLayer;
    }

}
