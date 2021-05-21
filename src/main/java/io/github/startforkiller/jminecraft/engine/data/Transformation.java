package io.github.startforkiller.jminecraft.engine.data;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    private final Matrix4f projectionMatrix;

    private final Matrix4f modelMatrix;

    private final Matrix4f viewMatrix;

    public Transformation() {
        modelMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public Matrix4f getModelMatrix(GameItem gameItem) {
        Vector3f rotation = gameItem.getRotation();
        modelMatrix.identity();/*.translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());*/
        return modelMatrix;
    }

    public Matrix4f getModelMatrix(Vector3f position) {
        modelMatrix.identity().translate(position);
        return modelMatrix;
    }

    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        viewMatrix.identity();
        viewMatrix.rotate((float)(rotation.x + (Camera.TAU / 4)), 0.0f, 1.0f, 0.0f);
        viewMatrix.rotate(-rotation.y, (float)Math.cos((float)(rotation.x + (Camera.TAU / 4))), 0.0f, (float)Math.sin((float)(rotation.x + (Camera.TAU / 4))));
        //viewMatrix.rotate((float)(rotation.x + (Camera.TAU / 4)), new Vector3f(1, 0, 0));
        //viewMatrix.rotate(rotation.y, new Vector3f(0, 1, 0));
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

}
