package io.github.startforkiller.jminecraft.game;

import io.github.startforkiller.jminecraft.engine.IGameLogic;
import io.github.startforkiller.jminecraft.engine.MouseInput;
import io.github.startforkiller.jminecraft.engine.data.*;
import io.github.startforkiller.jminecraft.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    private final Renderer renderer;

    private TextureManager textureManager;

    private BlockType grassBlock;
    private GameItem grassBlockItem;

    private final Camera camera;
    private final Vector3f cameraInc;

    public Game() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        textureManager = new TextureManager(16, 16, 256);

        grassBlock = new BlockType(textureManager, "grass", new HashMap<String, String>() {{
            put("top", "grass");
            put("bottom", "dirt");
            put("sides", "grass_side");
        }});

        textureManager.generateMipMaps();

        grassBlockItem = new GameItem(new Mesh(grassBlock.getVertexPositions(), grassBlock.getTexCoords(), grassBlock.getIndices(), EngineConstants.shadingValues));
        grassBlockItem.setPosition(0, 0, -3.0f);

        window.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            cameraInc.y = 1;
        }
    }

    @Override
    public void update(float delta, MouseInput mouseInput) {
        camera.movePosition(cameraInc.x * EngineConstants.CAMERA_POS_STEP,
                cameraInc.y * EngineConstants.CAMERA_POS_STEP,
                cameraInc.z * EngineConstants.CAMERA_POS_STEP);

        if(mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * EngineConstants.MOUSE_SENSITIVITY,
                    rotVec.y * EngineConstants.MOUSE_SENSITIVITY,
                    0);
        }
    }

    @Override
    public void render(Window window) {
        renderer.render(window, textureManager, new GameItem[]{
                grassBlockItem
        }, camera);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();

        grassBlockItem.getMesh().cleanup();
    }

}
