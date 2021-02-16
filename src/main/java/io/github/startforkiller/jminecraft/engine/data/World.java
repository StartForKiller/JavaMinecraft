package io.github.startforkiller.jminecraft.engine.data;

import io.github.startforkiller.jminecraft.engine.Window;
import io.github.startforkiller.jminecraft.engine.data.models.CubeModel;
import io.github.startforkiller.jminecraft.engine.data.models.PlantModel;
import io.github.startforkiller.jminecraft.game.Renderer;
import org.joml.Vector3f;

import java.util.*;

public class World {

    private TextureManager textureManager;
    public LinkedList<BlockType> blockTypes = new LinkedList<>();

    private Map<Vector3f, Chunk> chunks = new LinkedHashMap<>();

    public static long getUnsignedInt(int x) {
        return x & 0x00000000ffffffffL;
    }

    public World() {
        textureManager = new TextureManager(16, 16, 256);

        blockTypes.add(0, null);
        blockTypes.add(new BlockType(textureManager, "cobblestone", new HashMap<String, String>() {{
            put("all", "cobblestone");
        }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "grass", new HashMap<String, String>() {{
                    put("top", "grass");
                    put("bottom", "dirt");
                    put("sides", "grass_side");
                }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "grass_block", new HashMap<String, String>() {{
            put("all", "grass");
        }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "dirt", new HashMap<String, String>() {{
            put("all", "dirt");
        }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "stone", new HashMap<String, String>() {{
            put("all", "stone");
        }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "sand", new HashMap<String, String>() {{
            put("all", "sand");
        }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "planks", new HashMap<String, String>() {{
            put("all", "planks");
        }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "log", new HashMap<String, String>() {{
            put("top", "log_top");
            put("bottom", "log_top");
            put("sides", "log_side");
        }}, new CubeModel()));
        blockTypes.add(new BlockType(textureManager, "daisy", new HashMap<String, String>() {{
            put("all", "daisy");
        }}, new PlantModel()));
        blockTypes.add(new BlockType(textureManager, "rose", new HashMap<String, String>() {{
            put("all", "rose");
        }}, new PlantModel()));

        textureManager.generateMipMaps();

        int[] firstRnd = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 9, 10};
        int[] secondRnd = {0, 3};
        int[] thirdRnd = {0, 0, 1};
        Random random = new Random();

        for(int x = 0; x < 8; x++) {
            for(int z = 0; z < 8; z++) {
                Chunk currentChunk = new Chunk(this, new Vector3f(x - 4, -1, z - 4));

                for(int i = 0; i < 16; i++) {
                    for(int j = 0; j < 16; j++) {
                        for(int k = 0; k < 16; k++) {
                            if(j == 15) currentChunk.blocks[i][j][k] = firstRnd[random.nextInt(firstRnd.length)];
                            else if(j > 12) currentChunk.blocks[i][j][k] = secondRnd[random.nextInt(secondRnd.length)];
                            else currentChunk.blocks[i][j][k] = thirdRnd[random.nextInt(thirdRnd.length)];
                        }
                    }
                }
                currentChunk.updateMesh();

                chunks.put(currentChunk.chunkPosition, currentChunk);
            }
        }

        /*chunks.put(new Vector3f(0, 0, 0), new Chunk(this, new Vector3f(0, 0, 0)));
        Chunk chunk = chunks.get(new Vector3f(0, 0, 0));
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < 16; y++) {
                for(int z = 0; z < 16; z++) {
                    chunk.blocks[x][y][z] = 1;
                }
            }
        }*/

        //chunk.updateMesh();
    }

    public int getBlockNumber(float x, float y, float z) {
        Vector3f chunkPosition = new Vector3f((float)Math.floor(x / 16), (float)Math.floor(y / 16), (float)Math.floor(z / 16));

        if(chunks.get(chunkPosition) == null) return 0;

        return chunks.get(chunkPosition).blocks[(int)(getUnsignedInt((int)x) % 16)][(int)(getUnsignedInt((int)y) % 16)][(int)(getUnsignedInt((int)z) % 16)];
    }

    public void render(Renderer renderer, Window window, Camera camera) {
        renderer.render(window, textureManager, chunks, camera);
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }
}
