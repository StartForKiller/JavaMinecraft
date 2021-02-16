package io.github.startforkiller.jminecraft.engine.data.models;

public interface DataModel {

    public float[][] getVertexPositions();
    public float[][] getTexCoords();
    public float[][] getShadingValues();

    public boolean isTransparent();
    public boolean isCube();

}
