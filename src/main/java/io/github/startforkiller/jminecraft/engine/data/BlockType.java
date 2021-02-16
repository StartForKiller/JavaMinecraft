package io.github.startforkiller.jminecraft.engine.data;

import io.github.startforkiller.jminecraft.engine.data.EngineConstants;
import io.github.startforkiller.jminecraft.engine.data.TextureManager;
import io.github.startforkiller.jminecraft.engine.data.models.DataModel;

import java.util.Map;

public class BlockType {

    private String name;
    private float[][] vertexPositions;
    private float[][] texCoords;
    private float[][] shadingValues;

    private DataModel model;

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

    public BlockType(TextureManager textureManager, String name, Map<String, String> blockFacesTextures, DataModel model) {
        this.name = name;
        this.model = model;

        vertexPositions = model.getVertexPositions();
        texCoords = model.getTexCoords().clone();
        shadingValues = model.getShadingValues();

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

    private void setBlockFace(int face, int texture) {
        if(face > (model.getVertexPositions().length - 1)) return;

        this.texCoords[face] = this.texCoords[face].clone();

        for(int vertex = 0; vertex < 4; vertex++) {
            texCoords[face][vertex * 3 + 2] = texture;
        }
    }

    public String getName() {
        return name;
    }

    public float[][] getVertexPositions() {
        return vertexPositions;
    }

    public float[][] getTexCoords() {
        return texCoords;
    }

    public float[][] getShadingValues() {
        return shadingValues;
    }

    public boolean isTransparent() {
        return model.isTransparent();
    }

    public boolean isCube() {
        return model.isCube();
    }

}
