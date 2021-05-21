package io.github.startforkiller.jminecraft.engine.data;

import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class Chunk extends Mesh {

    public static int CHUNK_WIDTH = 16;
    public static int CHUNK_HEIGHT = 16;
    public static int CHUNK_LENGTH = 16;

    private LinkedList<Float> positions = new LinkedList<>();
    private LinkedList<Float> texCoords = new LinkedList<>();
    private LinkedList<Integer> indices = new LinkedList<>();
    private LinkedList<Float> shadingValues = new LinkedList<>();

    public final Vector3f chunkPosition;
    public final Vector3f realPosition;

    private int meshIndexCounter = 0;

    private World world;
    public int[][][] blocks;
    public byte[][][] lights; //Level of light, from 0 to 15 we only need a byte because we use 4 bits fo sunlight and 4 bits for block lights

    public Chunk(World world, Vector3f chunkPosition) {
        super(new float[]{}, new float[]{}, new int[]{}, new float[]{});

        this.world = world;
        this.chunkPosition = chunkPosition;
        this.realPosition = new Vector3f(chunkPosition).mul(CHUNK_WIDTH, CHUNK_HEIGHT, CHUNK_LENGTH);
        this.blocks = new int[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_LENGTH];
        this.lights = new byte[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_LENGTH];
        for(int i = 0; i < CHUNK_WIDTH; i++) {
            for(int j = 0; j < CHUNK_WIDTH; j++) {
                Arrays.fill(this.lights[i][j], (byte) 0);
            }
        }
    }

    public void addFace(int face, BlockType blockType, float x, float y, float z, Vector3f blockPos) {
        float[] vertexPositions = blockType.getVertexPositions()[face].clone();

        for (int i = 0; i < 4; i++) {
            vertexPositions[i * 3 + 0] += x;
            vertexPositions[i * 3 + 1] += y;
            vertexPositions[i * 3 + 2] += z;
        }

        for (int i = 0; i < vertexPositions.length; i++) this.positions.add(i, vertexPositions[i]);

        int[] indices = new int[] {0, 1, 2, 0, 2, 3};

        for (int i = 0; i < indices.length; i++) this.indices.add(i, indices[i] + meshIndexCounter);
        meshIndexCounter += 4;

        float[] texCoords = blockType.getTexCoords()[face];
        for (int i = 0; i < texCoords.length; i++) this.texCoords.add(i, texCoords[i]);

        //If our light value is maximum, use or value
        //If not, we use the adjacent values - 1

        int ourLightningValue = 15;
        if(blockType.isTransparent()) {
            ourLightningValue = getBlocklight((int)blockPos.x, (int)blockPos.y, (int)blockPos.z);
        }
        else if(getBlocklight((int)blockPos.x, (int)blockPos.y, (int)blockPos.z) != 15) {
            switch(face) {
                case 0: // X + 1
                    ourLightningValue = getBlocklight((int)blockPos.x + 1, (int)blockPos.y, (int)blockPos.z);
                    break;
                case 1: // X - 1
                    ourLightningValue = getBlocklight((int)blockPos.x - 1, (int)blockPos.y, (int)blockPos.z);
                    break;
                case 2: // Y + 1
                    ourLightningValue = getBlocklight((int)blockPos.x, (int)blockPos.y + 1, (int)blockPos.z);
                    break;
                case 3: // Y - 1
                    ourLightningValue = getBlocklight((int)blockPos.x, (int)blockPos.y - 1, (int)blockPos.z);
                    break;
                case 4: // Z + 1
                    ourLightningValue = getBlocklight((int)blockPos.x, (int)blockPos.y, (int)blockPos.z + 1);
                    break;
                case 5: // Z - 1
                    ourLightningValue = getBlocklight((int)blockPos.x, (int)blockPos.y, (int)blockPos.z - 1);
                    break;
            }
        }

        float[] shadingVals = blockType.getShadingValues()[face];
        for (int i = 0; i < shadingVals.length; i++) this.shadingValues.add(i, (float) (shadingVals[i] * Math.pow(0.8, (15 - ourLightningValue))));
    }

    public int getBlock(int x, int y, int z) {
        int blockTypeID = this.blocks[x][y][z];

        if(blockTypeID == 0) return 0;

        BlockType blockType = world.blockTypes.get(blockTypeID);
        if(blockType.isTransparent()) return 0;
        else return blockTypeID;
    }

    public static float[] convertFloats(LinkedList<Float> floats)
    {
        float[] ret = new float[floats.size()];
        Iterator<Float> iterator = floats.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next();
        }
        return ret;
    }

    public static int[] convertIntegers(LinkedList<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next();
        }
        return ret;
    }

    public void updateMesh() {
        this.positions.clear();
        this.texCoords.clear();
        this.indices.clear();
        this.shadingValues.clear();

        this.meshIndexCounter = 0;

        for(int localX = 0; localX < CHUNK_WIDTH; localX++) {
            for(int localY = 0; localY < CHUNK_HEIGHT; localY++) {
                for(int localZ = 0; localZ < CHUNK_LENGTH; localZ++) {
                    int blockNumber = this.blocks[localX][localY][localZ];

                    Vector3f localPos = new Vector3f(localX, localY, localZ);

                    if(blockNumber > 0) {
                        BlockType blockType = this.world.blockTypes.get(blockNumber);

                        float x = this.realPosition.x + localX;
                        float y = this.realPosition.y + localY;
                        float z = this.realPosition.z + localZ;

                        if(blockType.isCube()) {
                            if (localX == 0) {
                                if (getBlock(localX + 1, localY, localZ) == 0) addFace(0, blockType, x, y, z, localPos);
                                if (!this.world.IsOpaqueBlock(x - 1, y, z)) addFace(1, blockType, x, y, z, localPos);
                            } else {
                                if (localX == 15) {
                                    if (!this.world.IsOpaqueBlock(x + 1, y, z)) addFace(0, blockType, x, y, z, localPos);
                                } else {
                                    if (getBlock(localX + 1, localY, localZ) == 0) addFace(0, blockType, x, y, z, localPos);
                                }
                                if (getBlock(localX - 1, localY, localZ) == 0) addFace(1, blockType, x, y, z, localPos);
                            }

                            if (localY == 0) {
                                if (getBlock(localX, localY + 1, localZ) == 0) addFace(2, blockType, x, y, z, localPos);
                                if (!this.world.IsOpaqueBlock(x, y - 1, z)) addFace(3, blockType, x, y, z, localPos);
                            } else {
                                if (localY == 15) {
                                    if (!this.world.IsOpaqueBlock(x, y + 1, z)) addFace(2, blockType, x, y, z, localPos);
                                } else {
                                    if (getBlock(localX, localY + 1, localZ) == 0) addFace(2, blockType, x, y, z, localPos);
                                }
                                if (getBlock(localX, localY - 1, localZ) == 0) addFace(3, blockType, x, y, z, localPos);
                            }

                            if (localZ == 0) {
                                if (getBlock(localX, localY, localZ + 1) == 0) addFace(4, blockType, x, y, z, localPos);
                                if (!this.world.IsOpaqueBlock(x, y, z - 1)) addFace(5, blockType, x, y, z, localPos);
                            } else {
                                if (localZ == 15) {
                                    if (!this.world.IsOpaqueBlock(x, y, z + 1)) addFace(4, blockType, x, y, z, localPos);
                                } else {
                                    if (getBlock(localX, localY, localZ + 1) == 0) addFace(4, blockType, x, y, z, localPos);
                                }
                                if (getBlock(localX, localY, localZ - 1) == 0) addFace(5, blockType, x, y, z, localPos);
                            }
                        } else {
                            for(int i = 0; i < blockType.getVertexPositions().length; i++) {
                                addFace(i, blockType, x, y ,z, localPos);
                            }
                        }
                    }
                }
            }
        }

        /*float[] positions = new float[this.positions.size()];
        int index = 0;
        for (final Float value: this.positions) {
            positions[index++] = value;
        }

        float[] texCoords = new float[this.texCoords.size()];
        index = 0;
        for (final Float value: this.texCoords) {
            texCoords[index++] = value;
        }

        float[] shadingValues = new float[this.shadingValues.size()];
        index = 0;
        for (final Float value: this.shadingValues) {
            shadingValues[index++] = value;
        }

        int[] indicesValues = new int[this.indices.size()];
        index = 0;
        for (final Integer value: this.indices) {
            indicesValues[index++] = value;
        }*/

        updateBuffers(convertFloats(this.positions), convertFloats(this.texCoords), convertIntegers(this.indices), convertFloats(this.shadingValues));
    }

    byte getSunlight(int x, int y, int z) {
        return (byte) ((Byte.toUnsignedInt(this.lights[x][y][z]) >> 4) & 0xF);
    }

    void setSunlight(int x, int y, int z, byte value) {
        this.lights[x][y][z] = (byte) ((Byte.toUnsignedInt(this.lights[x][y][z]) & 0xF) | (value << 4));
    }

    byte getBlocklight(int x, int y, int z) { //We only support edge cases and out chunk blocks, not all chunk blocks
        if(x < 0 || x > 15 || y < 0 || y > 15 || z < 0 || z > 15) {
            int newChunkLx = (x < 0) ? 15 : (x > 15) ? 0 : x;
            int newChunkLy = (y < 0) ? 15 : (y > 15) ? 0 : y;
            int newChunkLz = (z < 0) ? 15 : (z > 15) ? 0 : z;
            int newChunkX = (int) (chunkPosition.x + ((x < 0) ? -1 : (x > 15) ? 1 : 0));
            int newChunkY = (int) (chunkPosition.y + ((y < 0) ? -1 : (y > 15) ? 1 : 0));
            int newChunkZ = (int) (chunkPosition.z + ((z < 0) ? -1 : (z > 15) ? 1 : 0));

            Chunk newChunk = world.getChunk(newChunkX, newChunkY, newChunkZ);
            if(newChunk == null) return 0;
            return newChunk.getBlocklight(newChunkLx, newChunkLy, newChunkLz);
        }

        return (byte) ((Byte.toUnsignedInt(this.lights[x][y][z])) & 0xF);
    }

    void setBlocklight(int x, int y, int z, byte value) {
        if(x < 0 || x > 15 || y < 0 || y > 15 || z < 0 || z > 15) {
            int newChunkLx = (x < 0) ? 15 : (x > 15) ? 0 : x;
            int newChunkLy = (y < 0) ? 15 : (y > 15) ? 0 : y;
            int newChunkLz = (z < 0) ? 15 : (z > 15) ? 0 : z;
            int newChunkX = (int) (chunkPosition.x + ((x < 0) ? -1 : (x > 15) ? 1 : 0));
            int newChunkY = (int) (chunkPosition.y + ((y < 0) ? -1 : (y > 15) ? 1 : 0));
            int newChunkZ = (int) (chunkPosition.z + ((z < 0) ? -1 : (z > 15) ? 1 : 0));

            Chunk newChunk = world.getChunk(newChunkX, newChunkY, newChunkZ);
            if(newChunk == null) return;
            newChunk.setBlocklight(newChunkLx, newChunkLy, newChunkLz, value);
            return;
        }

        this.lights[x][y][z] = (byte) ((Byte.toUnsignedInt(this.lights[x][y][z]) & 0xF0) | value);
    }

}
