package io.github.startforkiller.jminecraft.engine.data.models;

public class CubeModel implements DataModel {

    private static float[][] vertexPositions = new float[][]{
            { 0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f},
            {-0.5f,  0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,  0.5f},
            { 0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f},
            {-0.5f, -0.5f,  0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f},
            {-0.5f,  0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f},
            { 0.5f,  0.5f, -0.5f,  0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f}
    };

    private static float[][] texCoords = new float[][]{
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f}
    };

    private static float[][] shadingValues = new float[][] {
            {0.6f, 0.6f, 0.6f, 0.6f},
            {0.6f, 0.6f, 0.6f, 0.6f},
            {1.0f, 1.0f, 1.0f, 1.0f},
            {0.4f, 0.4f, 0.4f, 0.4f},
            {0.8f, 0.8f, 0.8f, 0.8f},
            {0.8f, 0.8f, 0.8f, 0.8f}
    };

    @Override
    public float[][] getVertexPositions() {
        return vertexPositions;
    }

    @Override
    public float[][] getTexCoords() {
        return texCoords;
    }

    @Override
    public float[][] getShadingValues() {
        return shadingValues;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public boolean isCube() {
        return true;
    }
}
