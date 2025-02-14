import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChunkGenerator {

    public Chunk spawnChunk;
    public boolean firstChunkSpawned;

    Map<Integer,Map<Integer,Chunk>> generatedChunksMap;
    ArrayList<Chunk> allChunksList;

    public static int CHUNK_SIZE = 16;
    public static int DUMMY_CHUNK_CHANCE_PERCENT = 0;
    public static int CROSS_CHUNK_BRANCHING_CHANCE_PERCENT = 50;

    public ChunkGenerator(){
        generatedChunksMap = new HashMap<>();
        allChunksList = new ArrayList<>();
        firstChunkSpawned = false;
    }

    public Chunk grabChunk(int atX, int atY) {
        HashMap<Integer, Chunk> xChunks;
        if (generatedChunksMap.get(atY) == null) {
            generatedChunksMap.put(atY, xChunks=new HashMap<>());
        } else xChunks = (HashMap<Integer, Chunk>) generatedChunksMap.get(atY);

        Chunk foundChunk;
        if (xChunks.get(atX) == null) {
            xChunks.put(atX, foundChunk=new Chunk(atX,atY, this));
        } else foundChunk = xChunks.get(atX);

        if (!allChunksList.contains(foundChunk))
            allChunksList.add(foundChunk);

        //make spawn chunk if it's the first chunk and not already set
        if (!firstChunkSpawned) {
            firstChunkSpawned = true;
            spawnChunk = foundChunk;
            System.out.println("Spawn Chunk Set: " + spawnChunk);
        }

        return foundChunk;
    }

    public boolean hasChunkBeenGeneratedAt(int atX, int atY) {
        return generatedChunksMap.containsKey(atY) && generatedChunksMap.get(atY).containsKey(atX);
    }

    public void listChunksThatExist() {
        if (allChunksList.isEmpty()) {
            System.out.println("No Chunks Currently Exist");
            return;
        }
        for (Chunk c: allChunksList) {
            System.out.print(c+", ");
        }
        System.out.println();
    }

}
