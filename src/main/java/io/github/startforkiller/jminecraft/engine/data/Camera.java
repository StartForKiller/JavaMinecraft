package io.github.startforkiller.jminecraft.engine.data;

import org.joml.Vector3f;

public class Camera {

    private final Vector3f position;
    private final Vector3f rotation;

    public static final double TAU = 6.28318530717958647692;

    public Camera() {
        position = new Vector3f(0, 0, 0);
        rotation = new Vector3f((float)(-TAU / 4), 0, 0);
    }

    public Camera(Vector3f position, Vector3f rotation) {
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ, float doubleSpeed) {
        float angle = (float)(rotation.x - Math.atan2(offsetZ, offsetX) + (TAU / 4));
        if(offsetX != 0.0f || offsetZ != 0.0f) {
            position.x += (float) Math.cos(angle) * doubleSpeed * EngineConstants.CAMERA_POS_STEP;
            position.z += (float) Math.sin(angle) * doubleSpeed * EngineConstants.CAMERA_POS_STEP;
        }

        position.y += offsetY * EngineConstants.CAMERA_POS_STEP;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y, float z) {
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ) {
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;

        rotation.y = Math.max((float)(-TAU / 4), Math.min((float)(TAU / 4), rotation.y));
    }

}
