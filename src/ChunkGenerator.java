import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChunkGenerator {

    Map<Integer,Map<Integer,Chunk>> generatedChunksMap;
    ArrayList<Chunk> allChunksList;
    public static int CHUNK_SIZE = 16;

    public ChunkGenerator(){
        generatedChunksMap = new HashMap<>();
        allChunksList = new ArrayList<>();
    }

    public Chunk grabChunk(int atX, int atY) {
        HashMap<Integer, Chunk> xChunks;
        if (generatedChunksMap.get(atY) == null) {
            generatedChunksMap.put(atY, xChunks=new HashMap<>());
        } else xChunks = (HashMap<Integer, Chunk>) generatedChunksMap.get(atY);

        Chunk foundChunk;
        if (xChunks.get(atX) == null) {
            xChunks.put(atX, foundChunk=new Chunk(atX,atY));
        } else foundChunk = xChunks.get(atX);

        if (!allChunksList.contains(foundChunk))
            allChunksList.add(foundChunk);

        return foundChunk;
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
