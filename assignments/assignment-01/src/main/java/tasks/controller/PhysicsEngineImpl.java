package tasks.controller;

import tasks.model.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhysicsEngineImpl implements PhysicsEngine {
    private final ExecutorService exec;
    private final int nTasks = Runtime.getRuntime().availableProcessors() * 2;

    public PhysicsEngineImpl() {
        this.exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    @Override
    public void update(BoardImpl board, GameStateManagerImpl stateManager, long dt) {
        if (stateManager.isGameOver()) {
            return;
        }
        board.getPlayerBall().updateState(dt, board);
        board.getBotBall().updateState(dt, board);
        synchronized(board.getBalls()) {
            board.getBalls().forEach(b -> b.updateState(dt, board));
        }
        board.getGrid().rebuild(board.getBalls());
        CountDownLatchImpl latch = new CountDownLatchImpl(nTasks);
        for(int i = 0; i < nTasks; i++){
            exec.execute(new ResolveCollisionsTask(board, stateManager.getMonitor(), i, nTasks, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        BallImpl.resolveCollision(board.getBotBall(), board.getPlayerBall(), "");
        stateManager.checkRules(board, this);
    }

    @Override
    public void shutdown() {
        exec.shutdown();
    }
}
