package io.github.startforkiller.jminecraft.engine.data;

import org.joml.Vector3f;

import java.util.Arrays;
import java.util.LinkedList;

public class Chunk extends Mesh {

    private LinkedList<Float> positions = null;
    private LinkedList<Float> texCoords = null;
    private LinkedList<Integer> indices = null;
    private LinkedList<Float> shadingValues = null;

    public final Vector3f chunkPosition;

    private int meshIndexCounter = 0;

    private World world;
    public int[][][] blocks;

    public Chunk(World world, Vector3f chunkPosition) {
        super(new float[]{}, new float[]{}, new int[]{}, new float[]{});

        this.world = world;
        this.chunkPosition = chunkPosition;
        this.blocks = new int[16][16][16];
    }

    public void addFace(int face, BlockType blockType, float x, float y, float z) {
        float[] vertexPositions = blockType.getVertexPositions()[face].clone();

        for (int i = 0; i < 4; i++) {
            vertexPositions[i * 3 + 0] += x;
            vertexPositions[i * 3 + 1] += y;
            vertexPositions[i * 3 + 2] += z;
        }

        for (int i = 0; i < vertexPositions.length; i++) this.positions.add(i, vertexPositions[i]);

        int[] indices = new int[] {0, 1, 2, 0, 2, 3};

        for (int i = 0; i < 6; i++) {
            indices[i] += meshIndexCounter;
        }

        for (int i = 0; i < indices.length; i++) this.indices.add(i, indices[i]);
        meshIndexCounter += 4;

        float[] texCoords = blockType.getTexCoords()[face];
        for (int i = 0; i < texCoords.length; i++) this.texCoords.add(i, texCoords[i]);

        float[] shadingVals = blockType.getShadingValues()[face];
        for (int i = 0; i < shadingVals.length; i++) this.shadingValues.add(i, shadingVals[i]);
    }

    public void updateMesh() {
        this.positions = new LinkedList<>();
        this.texCoords = new LinkedList<>();
        this.indices = new LinkedList<>();
        this.shadingValues = new LinkedList<>();

        this.meshIndexCounter = 0;

        for(int localX = 0; localX < 16; localX++) {
            for(int localY = 0; localY < 16; localY++) {
                for(int localZ = 0; localZ < 16; localZ++) {
                    int blockNumber = this.blocks[localX][localY][localZ];

                    if(blockNumber > 0) {
                        BlockType blockType = this.world.blockTypes.get(blockNumber);

                        float x = this.chunkPosition.x + localX;
                        float y = this.chunkPosition.y + localY;
                        float z = this.chunkPosition.z + localZ;

                        if(blockType.isCube()) {
                            if (localX == 0) {
                                if (this.blocks[localX + 1][localY][localZ] == 0) addFace(0, blockType, x, y, z);
                                if (this.world.getBlockNumber(x - 1, y, z) == 0) addFace(1, blockType, x, y, z);
                            } else {
                                if (localX == 15) {
                                    if (this.world.getBlockNumber(x + 1, y, z) == 0) addFace(0, blockType, x, y, z);
                                } else {
                                    if (this.blocks[localX + 1][localY][localZ] == 0) addFace(0, blockType, x, y, z);
                                }
                                if (this.blocks[localX - 1][localY][localZ] == 0) addFace(1, blockType, x, y, z);
                            }

                            if (localY == 0) {
                                if (this.blocks[localX][localY + 1][localZ] == 0) addFace(2, blockType, x, y, z);
                                if (this.world.getBlockNumber(x, y - 1, z) == 0) addFace(3, blockType, x, y, z);
                            } else {
                                if (localY == 15) {
                                    if (this.world.getBlockNumber(x, y + 1, z) == 0) addFace(2, blockType, x, y, z);
                                } else {
                                    if (this.blocks[localX][localY + 1][localZ] == 0) addFace(2, blockType, x, y, z);
                                }
                                if (this.blocks[localX][localY - 1][localZ] == 0) addFace(3, blockType, x, y, z);
                            }

                            if (localZ == 0) {
                                if (this.blocks[localX][localY][localZ + 1] == 0) addFace(4, blockType, x, y, z);
                                if (this.world.getBlockNumber(x, y, z - 1) == 0) addFace(5, blockType, x, y, z);
                            } else {
                                if (localZ == 15) {
                                    if (this.world.getBlockNumber(x, y, z + 1) == 0) addFace(4, blockType, x, y, z);
                                } else {
                                    if (this.blocks[localX][localY][localZ + 1] == 0) addFace(4, blockType, x, y, z);
                                }
                                if (this.blocks[localX][localY][localZ - 1] == 0) addFace(5, blockType, x, y, z);
                            }
                        } else {
                            for(int i = 0; i < blockType.getVertexPositions().length; i++) {
                                addFace(i, blockType, x, y ,z);
                            }
                        }
                    }
                }
            }
        }

        float[] positions = new float[this.positions.size()];
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
        }
        updateBuffers(positions, texCoords, indicesValues, shadingValues);
    }

}
