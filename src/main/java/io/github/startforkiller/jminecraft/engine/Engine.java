package io.github.startforkiller.jminecraft.engine;

import io.github.startforkiller.jminecraft.engine.data.EngineConstants;

public class Engine implements Runnable {

    private final Window window;
    private final Timer timer;
    private final IGameLogic gameLogic;

    private final MouseInput mouseInput;

    private double lastFps;
    private int fps;

    private String screenTitle;

    public Engine(String screenTitle, int width, int height, boolean vSync, IGameLogic gameLogic) throws Exception {
        this.screenTitle = screenTitle;

        window = new Window(this.screenTitle, width, height, vSync);
        this.gameLogic = gameLogic;
        timer = new Timer();
        mouseInput = new MouseInput();
    }

    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    protected void init() throws Exception {
        window.init();
        timer.init();
        mouseInput.init(window);
        gameLogic.init(window);
        lastFps = timer.getTime();
        fps = 0;
    }

    protected void gameLoop() {
        double elapsedTime;
        double accumulator = 0f;
        double interval = 1f / EngineConstants.TARGET_UPS;

        boolean running = true;
        while(running && !window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while(accumulator >= interval) {
                update((float)interval);
                accumulator -= interval;
            }

            render();

            if(!window.isvSync()) sync();
        }
    }

    private void sync() {
        double loopSlot = 1f / EngineConstants.TARGET_FPS;
        double endTime = timer.getLastTime() + loopSlot;
        while(timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {

            }
        }
    }

    protected void input() {
        mouseInput.input(window);
        gameLogic.input(window, mouseInput);
    }

    protected void update(float delta) {
        gameLogic.update(delta ,mouseInput);
    }

    protected void render() {
        if(timer.getLastTime() - lastFps > 1) {
            lastFps = timer.getLastTime();
            window.setScreenTitle(screenTitle + " - " + fps + " FPS");
            fps = 0;
        }
        fps++;
        gameLogic.render(window);
        window.update();
    }

    private void cleanup() {
        gameLogic.cleanup();
    }

}
