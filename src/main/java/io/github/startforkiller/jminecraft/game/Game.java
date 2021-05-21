package io.github.startforkiller.jminecraft.game;

import io.github.startforkiller.jminecraft.engine.IGameLogic;
import io.github.startforkiller.jminecraft.engine.MouseInput;
import io.github.startforkiller.jminecraft.engine.data.*;
import io.github.startforkiller.jminecraft.engine.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    private final Renderer renderer;

    private final Camera camera;
    private final Vector3f cameraInc;
    private boolean doubleSpeed = false;

    private GameItem[] gameItems;
    public World world;

    public static Game instance;
    public MouseInput mouseInput;
    public boolean activeMouse = false;

    public Game() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f();

        instance = this;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        this.world = new World();

        gameItems = new GameItem[] {
        };

        window.setClearColor(0.5f, 0.7f, 1.0f, 1.0f);
    }

    static int currentBlock = 13;

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = 1;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = -1;
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
        if(window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            doubleSpeed = true;
        }
        if(window.isKeyPressed(GLFW_KEY_KP_ADD)) {
            currentBlock++;
            if(instance.world.blockTypes.size() <= currentBlock) currentBlock = 0;
        }
        if(window.isKeyPressed(GLFW_KEY_KP_SUBTRACT)) {
            currentBlock--;
            if(currentBlock < 0) currentBlock = instance.world.blockTypes.size() - 1;
        }
    }

    static boolean lastClicked = false;

    public static Integer HitCallback(Vector3f[] blocks) {
        if(instance.mouseInput.isLeftButtonPressed()) {
            instance.mouseInput.flushLeft(); // To force the user to click multiple times for multiple blocks

            instance.world.SetBlock(blocks[0], currentBlock);
        } else if(instance.mouseInput.isRightButtonPressed()) {
            instance.mouseInput.flushRight(); // To force the user to click multiple times for multiple blocks

            instance.world.SetBlock(blocks[1], 0);
        } else if(instance.mouseInput.isMiddleButtonPressed()) {
            instance.mouseInput.flushMiddle(); // To force the user to click multiple times for multiple blocks

            currentBlock = instance.world.getBlockNumber(blocks[1].x, blocks[1].y, blocks[1].z);
        }

        return 0;
    }

    @Override
    public void update(float delta, MouseInput mouseInput, Window window) {
        this.mouseInput = mouseInput;

        camera.movePosition(cameraInc.x,
                cameraInc.y,
                cameraInc.z, doubleSpeed ? 2.0f : 1.0f);

        if(!activeMouse && (mouseInput.isRightButtonPressed() || mouseInput.isLeftButtonPressed())) {
            activeMouse = true;
        } else if(activeMouse) {
            glfwSetInputMode(window.getWindowHandle(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * EngineConstants.MOUSE_SENSITIVITY,
                    rotVec.y * EngineConstants.MOUSE_SENSITIVITY,
                    0);

            HitRay hitRay = new HitRay(new Vector3f(camera.getRotation()), new Vector3f(camera.getPosition()), world);

            while (hitRay.currDistance < hitRay.HIT_RANGE) {
                if (hitRay.step(Game::HitCallback)) break;
            }

            if(!instance.mouseInput.isLeftButtonPressed())
                lastClicked = false;
        }
    }

    @Override
    public void render(Window window) {
        if(gameItems.length != 0)
            renderer.render(window, world.getTextureManager() , gameItems, camera);

        world.render(renderer, window, camera);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();

        //TODO: world clear meshes
    }

}
