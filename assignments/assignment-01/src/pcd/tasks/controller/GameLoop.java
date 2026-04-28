package pcd.tasks.controller;

import pcd.tasks.model.Board;
import pcd.tasks.view.View;
import pcd.tasks.view.ViewModel;

public class GameLoop extends Thread {

    private ViewModel viewModel;
    private Board board;
    private View view;
    private GameStateManager gameStateManager;
    private BotController botController;
    private PhysicsEngine physicsEngine;

    public GameLoop(ViewModel viewModel, Board board, View view) {
        this.viewModel = viewModel;
        this.board = board;
        this.view = view;
        this.gameStateManager = new GameStateManager();
        this.botController = new BotController();
        this.physicsEngine = new PhysicsEngine();
    }

    public void run() {
        long t0 = System.currentTimeMillis();
        long lastUpdateTime = System.currentTimeMillis();
        int nFrames = 0;
        while (!gameStateManager.isGameOver()) {
            long elapsed = System.currentTimeMillis() - lastUpdateTime;
            lastUpdateTime = System.currentTimeMillis();
            botController.update(board);
            physicsEngine.update(board, gameStateManager, elapsed);
            nFrames++;
            int framePerSec = 0;
            long dt = (System.currentTimeMillis() - t0);
            if (dt > 0) {
                framePerSec = (int) (nFrames * 1000 / dt);
            }
            viewModel.update(board, gameStateManager, framePerSec);
            view.render();
        }
    }
}
