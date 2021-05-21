package io.github.startforkiller.jminecraft.engine.data;

import io.github.startforkiller.jminecraft.engine.Timer;
import io.github.startforkiller.jminecraft.engine.Window;
import io.github.startforkiller.jminecraft.engine.data.models.CactusModel;
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

    Queue<BFSLightNode> bfsLightNodeQueue = new LinkedList<>();
    Queue<BFSLightRemovalNode> bfsLightRemovalNodeQueue = new LinkedList<>();

    Queue<BFSLightNode> bfsSkyLightNodeQueue = new LinkedList<>();
    Queue<BFSLightRemovalNode> bfsSkyLightRemovalNodeQueue = new LinkedList<>();

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
        blockTypes.add(new BlockType(textureManager, "cactus", new HashMap<String, String>() {{
            put("top", "cactus_top");
            put("bottom", "cactus_bottom");
            put("sides", "cactus_side");
        }}, new CactusModel()));
        blockTypes.add(new BlockType(textureManager, "dead_bush", new HashMap<String, String>() {{
            put("all", "dead_bush");
        }}, new PlantModel()));
        blockTypes.add(new BlockType(textureManager, "lamp", new HashMap<String, String>() {{
            put("all", "lamp");
        }}, new CubeModel(), true));

        textureManager.generateMipMaps();

        int[] firstRnd = {0, 9, 10};
        Random random = new Random();

        for(int x = 0; x < 8; x++) {
            for(int z = 0; z < 8; z++) {
                Chunk currentChunk = new Chunk(this, new Vector3f(x - 4, -1, z - 4));

                for(int i = 0; i < Chunk.CHUNK_WIDTH; i++) {
                    for(int j = 0; j < Chunk.CHUNK_HEIGHT; j++) {
                        for(int k = 0; k < Chunk.CHUNK_LENGTH; k++) {
                            if(j == 15) currentChunk.blocks[i][j][k] = firstRnd[random.nextInt(firstRnd.length)];
                            else if(j == 14) currentChunk.blocks[i][j][k] = 2;
                            else if(j > 12) currentChunk.blocks[i][j][k] = 4;
                            else currentChunk.blocks[i][j][k] = 5;
                            //currentChunk.blocks[i][j][k] = 1;
                        }
                    }
                }

                chunks.put(currentChunk.chunkPosition, currentChunk);
            }
        }

        Timer timer = new Timer();
        timer.init();
        for(Chunk chunk : chunks.values()) {
            chunk.updateMesh();
        }

        float timeElapsed = timer.getElapsedTime();
        System.out.println(timeElapsed);
        System.out.println("Average: " + timeElapsed / chunks.size());
    }

    public Vector3f getChunkPosition(Vector3f position) {
        return new Vector3f((float)Math.floor(position.x / Chunk.CHUNK_WIDTH), (float)Math.floor(position.y / Chunk.CHUNK_HEIGHT), (float)Math.floor(position.z / Chunk.CHUNK_LENGTH));
    }

    public Vector3f getLocalPosition(Vector3f position) {
        Vector3f temp = getChunkPosition(position);

        Vector3f pos2 = new Vector3f(position.x - (temp.x * Chunk.CHUNK_WIDTH), position.y - (temp.y * Chunk.CHUNK_HEIGHT), position.z - (temp.z * Chunk.CHUNK_LENGTH));

        return pos2;
    }

    public int getBlockNumber(float x, float y, float z) {
        Vector3f chunkPosition = new Vector3f((float)Math.floor(x / Chunk.CHUNK_WIDTH), (float)Math.floor(y / Chunk.CHUNK_HEIGHT), (float)Math.floor(z / Chunk.CHUNK_LENGTH));

        if(chunks.get(chunkPosition) == null) {
            return 0;
        }

        Vector3f temp = getLocalPosition(new Vector3f(x, y, z));
        return chunks.get(chunkPosition).getBlock((int)temp.x, (int)temp.y, (int)temp.z);
    }

    public boolean IsOpaqueBlock(Vector3f position) {
        int blockNumber = getBlockNumber(position.x, position.y, position.z);
        if(blockTypes.contains(blockNumber)) {
            return !blockTypes.get(blockNumber).isTransparent();
        }

        return false;
    }

    public boolean IsOpaqueBlock(float x, float y, float z) {
        return IsOpaqueBlock(new Vector3f(x, y, z));
    }

    public void SetBlock(Vector3f position, int number) {
        Vector3f chunkPosition = getChunkPosition(position);

        Chunk currentChunk;
        if(!chunks.containsKey(chunkPosition)) {
            if(number == 0) return;

            currentChunk = new Chunk(this, chunkPosition);
            chunks.put(chunkPosition, currentChunk);
        } else {
            currentChunk = chunks.get(chunkPosition);
        }

        int lastBlockNumber = getBlockNumber(position.x, position.y, position.z);
        if(lastBlockNumber == number) return;

        Vector3f localPosition = getLocalPosition(position);
        int lx = (int) localPosition.x;
        int ly = (int) localPosition.y;
        int lz = (int) localPosition.z;

        currentChunk.blocks[lx][ly][lz] = number;

        int cx = (int) chunkPosition.x;
        int cy = (int) chunkPosition.y;
        int cz = (int) chunkPosition.z;

        if(lx == (Chunk.CHUNK_WIDTH - 1)) tryUpdateChunkMesh(new Vector3f(cx + 1, cy, cz));
        if(lx == 0) tryUpdateChunkMesh(new Vector3f(cx - 1, cy, cz));

        if(ly == (Chunk.CHUNK_HEIGHT - 1)) tryUpdateChunkMesh(new Vector3f(cx, cy + 1, cz));
        if(ly == 0) tryUpdateChunkMesh(new Vector3f(cx, cy - 1, cz));

        if(lz == (Chunk.CHUNK_LENGTH - 1)) tryUpdateChunkMesh(new Vector3f(cx, cy, cz + 1));
        if(lz == 0) tryUpdateChunkMesh(new Vector3f(cx, cy, cz - 1));

        boolean propagateLightIfNotPropagated = false;
        if(lastBlockNumber != 0 && blockTypes.get(lastBlockNumber).hasLight()) {
            bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly, lz, currentChunk.getBlocklight(lx, ly, lz), currentChunk));
            currentChunk.setBlocklight(lx, ly, lz, (byte) 0);

            updateBlockRemovalLights(currentChunk);

            propagateLightIfNotPropagated = true;
        }

        //Update lighting
        if(blockTypes.get(number) == null && !propagateLightIfNotPropagated) { //We broke a block :D calculate skylight too
            int currentLight = 0;
            currentLight = (getBlockNumber(position.x + 1, position.y, position.z) == 0 && (currentLight < currentChunk.getBlocklight(lx + 1, ly, lz))) ? currentChunk.getBlocklight(lx + 1, ly, lz) : currentLight;
            currentLight = (getBlockNumber(position.x - 1, position.y, position.z) == 0 && (currentLight < currentChunk.getBlocklight(lx - 1, ly, lz))) ? currentChunk.getBlocklight(lx - 1, ly, lz) : currentLight;
            currentLight = (getBlockNumber(position.x, position.y + 1, position.z) == 0 && (currentLight < currentChunk.getBlocklight(lx, ly + 1, lz))) ? currentChunk.getBlocklight(lx, ly + 1, lz) : currentLight;
            currentLight = (getBlockNumber(position.x, position.y - 1, position.z) == 0 && (currentLight < currentChunk.getBlocklight(lx, ly - 1, lz))) ? currentChunk.getBlocklight(lx, ly - 1, lz) : currentLight;
            currentLight = (getBlockNumber(position.x, position.y, position.z + 1) == 0 && (currentLight < currentChunk.getBlocklight(lx, ly, lz + 1))) ? currentChunk.getBlocklight(lx, ly, lz + 1) : currentLight;
            currentLight = (getBlockNumber(position.x, position.y, position.z - 1) == 0 && (currentLight < currentChunk.getBlocklight(lx, ly, lz - 1))) ? currentChunk.getBlocklight(lx, ly, lz - 1) : currentLight;

            if(currentLight > 0) {
                currentChunk.setBlocklight(lx, ly, lz, (byte)(currentLight - 1));
                bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz, currentChunk));

                updateBlockLights(currentChunk);
                updateRemovedBlocksAdjacentChunksBlockLights(lx, ly, lz, currentChunk.chunkPosition);
                propagateLightIfNotPropagated = false;
            }
        }
        else if(blockTypes.get(number) != null && blockTypes.get(number).hasLight()) {
            //Recalculate block lighting

            currentChunk.setBlocklight(lx, ly, lz, (byte) 15); //act like torches for now
            bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz, currentChunk));

            propagateLightIfNotPropagated = false;

            updateBlockLights(currentChunk);
        } else if(blockTypes.get(number) != null) {
            bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly, lz, currentChunk.getBlocklight(lx, ly, lz), currentChunk));
            currentChunk.setBlocklight(lx, ly, lz, (byte) 0);

            updateBlockRemovalLights(currentChunk);
            updateBlockLights(currentChunk);

            propagateLightIfNotPropagated = false;
        }

        if(propagateLightIfNotPropagated) {
            updateBlockLights(currentChunk);
        }

        currentChunk.updateMesh();

        //TODO: Recalculate skylight
    }

    private void tryUpdateChunkMesh(Vector3f chunkPosition) {
        if(chunks.containsKey(chunkPosition)) {
            chunks.get(chunkPosition).updateMesh();
        }
    }

    public void render(Renderer renderer, Window window, Camera camera) {
        renderer.render(window, textureManager, chunks, camera);
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    int getTopBlock(float x, float z) { //We go down to up
        Vector3f chunkPosition = getChunkPosition(new Vector3f((int)x, 0, (int)z));
        Chunk currentCheckedChunk = chunks.get(chunkPosition);
        int currentTopValue = -1;
        while(true) {
            if(currentCheckedChunk == null) break;

            currentCheckedChunk = chunks.get(chunkPosition.add(0, 1, 0)); //We go up
            currentTopValue += 16;
        }
        currentCheckedChunk = chunks.get(chunkPosition.add(0, -1, 0));

        while(true) {
            if(currentCheckedChunk == null) break;

            for (int i = (Chunk.CHUNK_HEIGHT - 1); i >= 0; --i) {
                if (!blockTypes.get(currentCheckedChunk.getBlock((int) x, i, (int) z)).isTransparent()) {
                    return currentTopValue;
                }
                currentTopValue--;
            }

            currentCheckedChunk = chunks.get(chunkPosition.add(0, -1, 0));
        }

        return -1;
    }

    boolean blockIsTransparentOrAir(int x, int y, int z, Chunk chunk) {
        BlockType type = blockTypes.get(chunk.getBlock(x, y, z));
        if(type == null) return true;
        if(type.hasLight()) return false; //Well, this isn't needed but double check.
        return type.isTransparent();
    }

    Chunk getChunk(Vector3f pos) {
        return getChunk((int)pos.x, (int)pos.y, (int)pos.z);
    }

    Chunk getChunk(int x, int y, int z) {
        return chunks.get(new Vector3f(x, y, z));
    }

    class BFSLightNode {
        BFSLightNode(int x, int y, int z, Chunk chunk) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.chunk = chunk;
        }

        public int x;
        public int y;
        public int z;
        public Chunk chunk;
    }

    void updateBlockLights(Chunk updatingChunk) {
        Set<Chunk> affectedChunks = new LinkedHashSet<>();

        while(!bfsLightNodeQueue.isEmpty()) {
            BFSLightNode currentNode = bfsLightNodeQueue.poll();

            int lightLevel = currentNode.chunk.getBlocklight(currentNode.x, currentNode.y, currentNode.z);
            int lx = currentNode.x;
            int ly = currentNode.y;
            int lz = currentNode.z;

            if((lx - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(-1, 0, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(-1, 0, 0));
                if(blockIsTransparentOrAir(15, ly, lz, currChunk) && (currChunk.getBlocklight(15, ly, lz) + 2) <= lightLevel) {
                    //Propagate

                    currChunk.setBlocklight(15, ly, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(15, ly, lz, currChunk));
                    affectedChunks.add(currChunk);
                }
            } else if((lx - 1) >= 0) {
                if(blockIsTransparentOrAir(lx - 1, ly, lz, currentNode.chunk) && (currentNode.chunk.getBlocklight(lx - 1, ly, lz) + 2) <= lightLevel) {
                    //Propagate

                    currentNode.chunk.setBlocklight(lx - 1, ly, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx - 1, ly, lz, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                }
            }
            if((ly - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, -1, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, -1, 0));
                if(blockIsTransparentOrAir(lx, 15, lz, currChunk) && (currChunk.getBlocklight(lx, 15, lz) + 2) <= lightLevel) {
                    //Propagate

                    currChunk.setBlocklight(lx, 15, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, 15, lz, currChunk));
                    affectedChunks.add(currChunk);
                }
            } else if((ly - 1) >= 0) {
                if(blockIsTransparentOrAir(lx, ly - 1, lz, currentNode.chunk) && (currentNode.chunk.getBlocklight(lx, ly - 1, lz) + 2) <= lightLevel) {
                    //Propagate

                    currentNode.chunk.setBlocklight(lx, ly - 1, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly - 1, lz, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                }
            }
            if((lz - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, -1)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, -1));
                if(blockIsTransparentOrAir(lx, ly, 15, currChunk) && (currChunk.getBlocklight(lx, ly, 15) + 2) <= lightLevel) {
                    //Propagate

                    currChunk.setBlocklight(lx, ly, 15, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, 15, currChunk));
                    affectedChunks.add(currChunk);
                }
            } else if((lz - 1) >= 0) {
                if(blockIsTransparentOrAir(lx, ly, lz - 1, currentNode.chunk) && (currentNode.chunk.getBlocklight(lx, ly, lz - 1) + 2) <= lightLevel) {
                    //Propagate

                    currentNode.chunk.setBlocklight(lx, ly, lz - 1, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz - 1, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                }
            }
            if((lx + 1) > 15 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(+1, 0, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(+1, 0, 0));
                if(blockIsTransparentOrAir(0, ly, lz, currChunk) && (currChunk.getBlocklight(0, ly, lz) + 2) <= lightLevel) {
                    //Propagate

                    currChunk.setBlocklight(0, ly, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(0, ly, lz, currChunk));
                    affectedChunks.add(currChunk);
                }
            } else if((lx + 1) < 16) {
                if(blockIsTransparentOrAir(lx + 1, ly, lz, currentNode.chunk) && (currentNode.chunk.getBlocklight(lx + 1, ly, lz) + 2) <= lightLevel) {
                    //Propagate

                    currentNode.chunk.setBlocklight(lx + 1, ly, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx + 1, ly, lz, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                }
            }
            if((ly + 1) > 15) { //Edge: check another chunk
                Vector3f chunkPosTemp = new Vector3f(currentNode.chunk.chunkPosition).add(0, 1, 0);
                if(chunks.get(chunkPosTemp) == null) {
                    chunks.put(chunkPosTemp, new Chunk(this, chunkPosTemp));
                }
                Chunk currChunk = chunks.get(chunkPosTemp);
                if(blockIsTransparentOrAir(lx, 0, lz, currChunk) && (currChunk.getBlocklight(lx, 0, lz) + 2) <= lightLevel) {
                    //Propagate

                    currChunk.setBlocklight(lx, 0, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, 0, lz, currChunk));
                    affectedChunks.add(currChunk);
                }
            } else if((ly + 1) < 16) {
                if(blockIsTransparentOrAir(lx, ly + 1, lz, currentNode.chunk) && (currentNode.chunk.getBlocklight(lx, ly + 1, lz) + 2) <= lightLevel) {
                    //Propagate

                    currentNode.chunk.setBlocklight(lx, ly + 1, lz, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly + 1, lz, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                }
            }
            if((lz + 1) > 15 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, 1)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, 1));
                if(blockIsTransparentOrAir(lx, ly, 0, currChunk) && (currChunk.getBlocklight(lx, ly, 0) + 2) <= lightLevel) {
                    //Propagate

                    currChunk.setBlocklight(lx, ly, 0, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, 0, currChunk));
                    affectedChunks.add(currChunk);
                }
            } else if((lz + 1) < 16) {
                if(blockIsTransparentOrAir(lx, ly, lz + 1, currentNode.chunk) && (currentNode.chunk.getBlocklight(lx, ly, lz + 1) + 2) <= lightLevel) {
                    //Propagate

                    currentNode.chunk.setBlocklight(lx, ly, lz + 1, (byte) (lightLevel - 1));

                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz + 1, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                }
            }
        }

        for(Chunk affectedChunk : affectedChunks) {
            if(!updatingChunk.equals(affectedChunk)) affectedChunk.updateMesh();
        }
        affectedChunks.clear();
    }

    void updateRemovedBlocksAdjacentChunksBlockLights(int x, int y, int z, Vector3f chunkPos) {
        Set<Chunk> affectedChunks = new LinkedHashSet<>();
        if(x == 0) {
            affectedChunks.add(getChunk((int)(chunkPos.x - 1), (int)chunkPos.y, (int)chunkPos.z));
        } else if(x == 15) {
            affectedChunks.add(getChunk((int)(chunkPos.x + 1), (int)chunkPos.y, (int)chunkPos.z));
        }
        if(y == 0) {
            affectedChunks.add(getChunk((int)chunkPos.x, (int)(chunkPos.y - 1), (int)chunkPos.z));
        } else if(y == 15) {
            affectedChunks.add(getChunk((int)chunkPos.x, (int)(chunkPos.y + 1), (int)chunkPos.z));
        }
        if(z == 0) {
            affectedChunks.add(getChunk((int)chunkPos.x, (int)chunkPos.y, (int)(chunkPos.z - 1)));
        } else if(z == 15) {
            affectedChunks.add(getChunk((int)chunkPos.x, (int)chunkPos.y, (int)(chunkPos.z + 1)));
        }

        //Resolve lighting problems
        for(Chunk affectedChunk : affectedChunks) {
            if(affectedChunk != null) affectedChunk.updateMesh();
        }

        affectedChunks.clear();
    }

    class BFSLightRemovalNode {
        BFSLightRemovalNode(int x, int y, int z, int value, Chunk chunk) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.value = value;
            this.chunk = chunk;
        }

        public int x;
        public int y;
        public int z;
        public int value;
        public Chunk chunk;
    }

    void updateBlockRemovalLights(Chunk updatingChunk) {
        Set<Chunk> affectedChunks = new LinkedHashSet<>();

        while(!bfsLightRemovalNodeQueue.isEmpty()) {
            BFSLightRemovalNode currentNode = bfsLightRemovalNodeQueue.poll();

            int lx = currentNode.x;
            int ly = currentNode.y;
            int lz = currentNode.z;

            if((lx - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(-1, 0, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(-1, 0, 0));
                int neightbourLevel = currChunk.getBlocklight(15, ly, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currChunk.setBlocklight(15, ly, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(15, ly, lz, neightbourLevel, currChunk));
                    affectedChunks.add(currChunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(15, ly, lz, currChunk));
                }
            } else if((lx - 1) >= 0) {
                int neightbourLevel = currentNode.chunk.getBlocklight(lx - 1, ly, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currentNode.chunk.setBlocklight(lx - 1, ly, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx - 1, ly, lz, neightbourLevel, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx - 1, ly, lz, currentNode.chunk));
                }
            }
            if((ly - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, -1, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, -1, 0));
                int neightbourLevel = currChunk.getBlocklight(lx, 15, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currChunk.setBlocklight(lx, 15, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, 15, lz, neightbourLevel, currChunk));
                    affectedChunks.add(currChunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, 15, lz, currChunk));
                }
            } else if((ly - 1) >= 0) {
                int neightbourLevel = currentNode.chunk.getBlocklight(lx, ly - 1, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currentNode.chunk.setBlocklight(lx, ly - 1, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly - 1, lz, neightbourLevel, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly - 1, lz, currentNode.chunk));
                }
            }
            if((lz - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, -1)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, -1));
                int neightbourLevel = currChunk.getBlocklight(lx, ly, 15);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currChunk.setBlocklight(lx, ly, 15, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly, 15, neightbourLevel, currChunk));
                    affectedChunks.add(currChunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, 15, currChunk));
                }
            } else if((lz - 1) >= 0) {
                int neightbourLevel = currentNode.chunk.getBlocklight(lx , ly, lz - 1);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currentNode.chunk.setBlocklight(lx, ly, lz - 1, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly, lz - 1, neightbourLevel, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz - 1, currentNode.chunk));
                }
            }
            if((lx + 1) > 15 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(+1, 0, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(+1, 0, 0));
                int neightbourLevel = currChunk.getBlocklight(0, ly, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currChunk.setBlocklight(0, ly, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(0, ly, lz, neightbourLevel, currChunk));
                    affectedChunks.add(currChunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(0, ly, lz, currChunk));
                }
            } else if((lx + 1) < 16) {
                int neightbourLevel = currentNode.chunk.getBlocklight(lx + 1, ly, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currentNode.chunk.setBlocklight(lx + 1, ly, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx + 1, ly, lz, neightbourLevel, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx + 1, ly, lz, currentNode.chunk));
                }
            }
            if((ly + 1) > 15 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 1, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 1, 0));
                int neightbourLevel = currChunk.getBlocklight(lx, 0, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currChunk.setBlocklight(lx, 0, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, 0, lz, neightbourLevel, currChunk));
                    affectedChunks.add(currChunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, 0, lz, currChunk));
                }
            } else if((ly + 1) < 16) {
                int neightbourLevel = currentNode.chunk.getBlocklight(lx , ly + 1, lz);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currentNode.chunk.setBlocklight(lx, ly + 1, lz, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly + 1, lz, neightbourLevel, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly + 1, lz, currentNode.chunk));
                }
            }
            if((lz + 1) > 15 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, 1)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, 1));
                int neightbourLevel = currChunk.getBlocklight(lx, ly, 0);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currChunk.setBlocklight(lx, ly, 0, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly, 0, neightbourLevel, currChunk));
                    affectedChunks.add(currChunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, 0, currChunk));
                }
            } else if((lz + 1) < 16) {
                int neightbourLevel = currentNode.chunk.getBlocklight(lx , ly, lz + 1);
                if(neightbourLevel != 0 && neightbourLevel < currentNode.value) {
                    currentNode.chunk.setBlocklight(lx, ly, lz + 1, (byte) 0);

                    bfsLightRemovalNodeQueue.add(new BFSLightRemovalNode(lx, ly, lz + 1, neightbourLevel, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                } else if(neightbourLevel >= currentNode.value) {
                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz + 1, currentNode.chunk));
                }
            }
        }

        for(Chunk affectedChunk : affectedChunks) {
            if(!updatingChunk.equals(affectedChunk)) affectedChunk.updateMesh();
        }
        affectedChunks.clear();
    }

    void updateSkyLights(Chunk updatingChunk) {
        Set<Chunk> affectedChunks = new LinkedHashSet<>();

        boolean hittedVoxel = false;
        while(!bfsSkyLightNodeQueue.isEmpty()) {
            BFSLightNode currentNode = bfsSkyLightNodeQueue.poll();

            int lightLevel = currentNode.chunk.getSunlight(currentNode.x, currentNode.y, currentNode.z);
            int lx = currentNode.x;
            int ly = currentNode.y;
            int lz = currentNode.z;

            if((ly - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, -1, 0)) != null) { //Edge: check another chunk
                Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, -1, 0));
                if(blockIsTransparentOrAir(lx, 15, lz, currChunk) && (currChunk.getSunlight(lx, 15, lz) + 2) <= lightLevel) {
                    //Propagate

                    currChunk.setSunlight(lx, 15, lz, (byte) lightLevel);

                    bfsLightNodeQueue.add(new BFSLightNode(lx, 15, lz, currChunk));
                    affectedChunks.add(currChunk);
                } else {
                    hittedVoxel = true;
                }
            } else if((ly - 1) >= 0) {
                if(blockIsTransparentOrAir(lx, ly - 1, lz, currentNode.chunk) && (currentNode.chunk.getSunlight(lx, ly - 1, lz) + 2) <= lightLevel) {
                    //Propagate

                    currentNode.chunk.setSunlight(lx, ly - 1, lz, (byte) lightLevel);

                    bfsLightNodeQueue.add(new BFSLightNode(lx, ly - 1, lz, currentNode.chunk));
                    affectedChunks.add(currentNode.chunk);
                } else {
                    hittedVoxel = true;
                }
            }
            if(hittedVoxel) {
                if ((lx - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(-1, 0, 0)) != null) { //Edge: check another chunk
                    Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(-1, 0, 0));
                    if (blockIsTransparentOrAir(15, ly, lz, currChunk) && (currChunk.getSunlight(15, ly, lz) + 2) <= lightLevel) {
                        //Propagate

                        currChunk.setSunlight(15, ly, lz, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(15, ly, lz, currChunk));
                        affectedChunks.add(currChunk);
                    }
                } else if ((lx - 1) >= 0) {
                    if (blockIsTransparentOrAir(lx - 1, ly, lz, currentNode.chunk) && (currentNode.chunk.getSunlight(lx - 1, ly, lz) + 2) <= lightLevel) {
                        //Propagate

                        currentNode.chunk.setSunlight(lx - 1, ly, lz, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(lx - 1, ly, lz, currentNode.chunk));
                        affectedChunks.add(currentNode.chunk);
                    }
                }
                if ((lz - 1) < 0 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, -1)) != null) { //Edge: check another chunk
                    Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, -1));
                    if (blockIsTransparentOrAir(lx, ly, 15, currChunk) && (currChunk.getSunlight(lx, ly, 15) + 2) <= lightLevel) {
                        //Propagate

                        currChunk.setSunlight(lx, ly, 15, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(lx, ly, 15, currChunk));
                        affectedChunks.add(currChunk);
                    }
                } else if ((lz - 1) >= 0) {
                    if (blockIsTransparentOrAir(lx, ly, lz - 1, currentNode.chunk) && (currentNode.chunk.getSunlight(lx, ly, lz - 1) + 2) <= lightLevel) {
                        //Propagate

                        currentNode.chunk.setSunlight(lx, ly, lz - 1, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz - 1, currentNode.chunk));
                        affectedChunks.add(currentNode.chunk);
                    }
                }
                if ((lx + 1) > 15 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(+1, 0, 0)) != null) { //Edge: check another chunk
                    Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(+1, 0, 0));
                    if (blockIsTransparentOrAir(0, ly, lz, currChunk) && (currChunk.getSunlight(0, ly, lz) + 2) <= lightLevel) {
                        //Propagate

                        currChunk.setSunlight(0, ly, lz, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(0, ly, lz, currChunk));
                        affectedChunks.add(currChunk);
                    }
                } else if ((lx + 1) < 16) {
                    if (blockIsTransparentOrAir(lx + 1, ly, lz, currentNode.chunk) && (currentNode.chunk.getSunlight(lx + 1, ly, lz) + 2) <= lightLevel) {
                        //Propagate

                        currentNode.chunk.setSunlight(lx + 1, ly, lz, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(lx + 1, ly, lz, currentNode.chunk));
                        affectedChunks.add(currentNode.chunk);
                    }
                }
                if ((lz + 1) > 15 && chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, 1)) != null) { //Edge: check another chunk
                    Chunk currChunk = chunks.get(new Vector3f(currentNode.chunk.chunkPosition).add(0, 0, 1));
                    if (blockIsTransparentOrAir(lx, ly, 0, currChunk) && (currChunk.getSunlight(lx, ly, 0) + 2) <= lightLevel) {
                        //Propagate

                        currChunk.setSunlight(lx, ly, 0, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(lx, ly, 0, currChunk));
                        affectedChunks.add(currChunk);
                    }
                } else if ((lz + 1) < 16) {
                    if (blockIsTransparentOrAir(lx, ly, lz + 1, currentNode.chunk) && (currentNode.chunk.getSunlight(lx, ly, lz + 1) + 2) <= lightLevel) {
                        //Propagate

                        currentNode.chunk.setSunlight(lx, ly, lz + 1, (byte) (lightLevel - 1));

                        bfsLightNodeQueue.add(new BFSLightNode(lx, ly, lz + 1, currentNode.chunk));
                        affectedChunks.add(currentNode.chunk);
                    }
                }
            }
        }

        for(Chunk affectedChunk : affectedChunks) {
            if(!updatingChunk.equals(affectedChunk)) affectedChunk.updateMesh();
        }
        affectedChunks.clear();
    }

}
