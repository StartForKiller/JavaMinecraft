package io.github.startforkiller.jminecraft.game;

import io.github.startforkiller.jminecraft.engine.data.*;
import io.github.startforkiller.jminecraft.engine.Utils;
import io.github.startforkiller.jminecraft.engine.Window;
import org.joml.Matrix4f;

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

        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("textureArraySampler");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, TextureManager textureManager,  GameItem[] gameItems, Camera camera) {
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
        for(GameItem gameItem : gameItems) {
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);

            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            gameItem.getMesh().render();
        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        if(shaderProgram != null) shaderProgram.cleanup();
    }

}
