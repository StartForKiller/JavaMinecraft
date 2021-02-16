package io.github.startforkiller.jminecraft.game;

import io.github.startforkiller.jminecraft.engine.data.*;
import io.github.startforkiller.jminecraft.engine.Utils;
import io.github.startforkiller.jminecraft.engine.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private ShaderProgram shaderProgram;

    private Transformation transformation;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init(Window window) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResource("/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResource("/fragment.glsl"));
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");

        shaderProgram.createUniform("modelMatrix");
        shaderProgram.createUniform("viewMatrix");
        shaderProgram.createUniform("textureArraySampler");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    private void renderCommonPart(Window window, TextureManager textureManager, Camera camera) {
        clear();

        if(window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix(EngineConstants.FOV, window.getWidth(), window.getHeight(), EngineConstants.Z_NEAR, EngineConstants.Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureManager.getTextureArray());
        shaderProgram.setUniform("textureArraySampler", 0);

        Matrix4f viewMatrix = transformation.getViewMatrix(camera);
        shaderProgram.setUniform("viewMatrix", viewMatrix);
    }

    public void render(Window window, TextureManager textureManager, GameItem[] gameItems, Camera camera) {
        renderCommonPart(window, textureManager, camera);

        for(GameItem gameItem : gameItems) {
            Matrix4f modelMatrix = transformation.getModelMatrix(gameItem);

            shaderProgram.setUniform("modelMatrix", modelMatrix);
            gameItem.getMesh().render();
        }

        shaderProgram.unbind();
    }

    public final static Matrix4f identityMatrix = new Matrix4f().identity();

    public void render(Window window, TextureManager textureManager, Map<Vector3f, Chunk> chunks, Camera camera) {
        renderCommonPart(window, textureManager, camera);

        for(Chunk chunk : chunks.values()) {
            shaderProgram.setUniform("modelMatrix", identityMatrix);
            chunk.render();
        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        if(shaderProgram != null) shaderProgram.cleanup();
    }

}
