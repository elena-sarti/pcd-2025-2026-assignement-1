package tasks.controller;

import tasks.model.BoardImpl;
import tasks.model.PhysicsEngineImpl;
import tasks.view.ViewImpl;
import tasks.view.ViewModelImpl;

public class GameLoop extends Thread {

    private final ViewModelImpl viewModel;
    private final BoardImpl board;
    private final ViewImpl view;
    private final GameStateManagerImpl gameStateManager;
    private final BotControllerImpl botController;
    private final PhysicsEngineImpl physicsEngine;

    public GameLoop(ViewModelImpl viewModel, BoardImpl board, ViewImpl view) {
        this.viewModel = viewModel;
        this.board = board;
        this.view = view;
        this.gameStateManager = new GameStateManagerImpl();
        this.botController = new BotControllerImpl();
        this.physicsEngine = new PhysicsEngineImpl();
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
