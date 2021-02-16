package io.github.startforkiller.jminecraft.engine.data.models;

public class PlantModel implements DataModel {

    private static final float[][] vertexPositions = new float[][]{
            {-0.3536f, 0.5000f,  0.3536f, -0.3536f, -0.5000f,  0.3536f,  0.3536f, -0.5000f, -0.3536f,  0.3536f, 0.5000f, -0.3536f},
            {-0.3536f, 0.5000f, -0.3536f, -0.3536f, -0.5000f, -0.3536f,  0.3536f, -0.5000f,  0.3536f,  0.3536f, 0.5000f,  0.3536f},
            { 0.3536f, 0.5000f, -0.3536f,  0.3536f, -0.5000f, -0.3536f, -0.3536f, -0.5000f,  0.3536f, -0.3536f, 0.5000f,  0.3536f},
            { 0.3536f, 0.5000f,  0.3536f,  0.3536f, -0.5000f,  0.3536f, -0.3536f, -0.5000f, -0.3536f, -0.3536f, 0.5000f, -0.3536f},
    };

    private static final float[][] texCoords = new float[][]{
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f},
            {0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f}
    };

    private static final float[][] shadingValues = new float[][] {
            {1.0f, 1.0f, 1.0f, 1.0f},
            {1.0f, 1.0f, 1.0f, 1.0f},
            {1.0f, 1.0f, 1.0f, 1.0f},
            {1.0f, 1.0f, 1.0f, 1.0f}
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
        return true;
    }

    @Override
    public boolean isCube() {
        return false;
    }
}
