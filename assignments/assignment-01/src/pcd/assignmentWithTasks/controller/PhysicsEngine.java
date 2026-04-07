package pcd.assignmentWithTasks.controller;

import pcd.assignmentWithTasks.model.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhysicsEngine {
    private final ExecutorService exec;
    private final int nTasks = Runtime.getRuntime().availableProcessors() * 4;

    public PhysicsEngine() {
        this.exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    public void update(Board board, GameStateManager stateManager, long dt) {
        if (stateManager.isGameOver()) {
            return;
        }
        board.getPlayerBall().updateState(dt, board);
        board.getBotBall().updateState(dt, board);
        synchronized(board.getBalls()) {
            board.getBalls().forEach(b -> b.updateState(dt, board));
        }
        board.getGrid().rebuild(board.getBalls());
        CountDownLatch latch = new CountDownLatch(nTasks);
        for(int i = 0; i < nTasks; i++){
            exec.execute(new ResolveCollisionsTask(board, stateManager.getMonitor(), i, nTasks, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Ball.resolveCollision(board.getBotBall(), board.getPlayerBall(), "");
        stateManager.checkRules(board, this);
    }

    public void shutdown() {
        exec.shutdown();
    }
}
