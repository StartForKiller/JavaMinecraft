package io.github.startforkiller.jminecraft.game;

import io.github.startforkiller.jminecraft.engine.Engine;
import io.github.startforkiller.jminecraft.engine.IGameLogic;

public class Main {

    public static void main(String[] args) {
        try {
            boolean vSync = true;
            IGameLogic gameLogic = new Game();
            Engine game = new Engine("Minecraft", 1024, 768, vSync, gameLogic);
            game.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
