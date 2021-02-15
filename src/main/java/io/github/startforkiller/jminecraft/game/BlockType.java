package io.github.startforkiller.jminecraft.game;

import io.github.startforkiller.jminecraft.engine.data.EngineConstants;
import io.github.startforkiller.jminecraft.engine.data.TextureManager;

import java.util.Map;

public class BlockType {

    private String name;
    private float[] vertexPositions;
    private int[] indices;
    private float[] texCoords;

    private static final String[] staticFaces = new String[] {
            "right", "left", "top", "bottom", "front", "back"
    };

    private static int getSideIndex(String side) {
        for(int i = 0; i < staticFaces.length; i++) {
            if(staticFaces[i].compareTo(side) == 0) return i;
        }

        //Unreachable
        return -1;
    }

    public BlockType(TextureManager textureManager, String name, Map<String, String> blockFacesTextures) {
        this.name = name;

        vertexPositions = EngineConstants.vertexPositions;
        indices = EngineConstants.indices;
        texCoords = EngineConstants.texCoords;

        blockFacesTextures.forEach((key, texture) -> {
            try {
                textureManager.addTexture(texture);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-1);
            }

            int textureIndex = textureManager.getTextureIndex(texture);

            switch(key) {
                case "all":
                    for(int i = 0; i < 6; i++) setBlockFace(i, textureIndex);
                    break;
                case "sides": {
                    setBlockFace(0, textureIndex);
                    setBlockFace(1, textureIndex);
                    setBlockFace(4, textureIndex);
                    setBlockFace(5, textureIndex);
                }
                break;
                default:
                    setBlockFace(getSideIndex(key), textureIndex);
                    break;
            }
        });
    }

    private void setBlockFace(int side, int texture) {
        for(int vertex = 0; vertex < 4; vertex++) {
            texCoords[side * 12 + vertex * 3 + 2] = texture;
        }
    }

    public String getName() {
        return name;
    }

    public float[] getVertexPositions() {
        return vertexPositions;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getTexCoords() {
        return texCoords;
    }
}
