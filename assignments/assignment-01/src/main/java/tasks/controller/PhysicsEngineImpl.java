package tasks.controller;

import tasks.model.BoardImpl;
import tasks.model.CountDownLatchImpl;
import tasks.model.ResolveCollisionsTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhysicsEngineImpl implements PhysicsEngine {

    private final ExecutorService exec;
    private final int nTasks = Runtime.getRuntime().availableProcessors() * 2;

    public PhysicsEngineImpl() {
        this.exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    }

    @Override
    public void update(BoardImpl board, long dt) {
        board.getPlayerBall().updateState(dt, board);
        board.getBotBall().updateState(dt, board);
        board.getBalls().forEach(b -> b.updateState(dt, board));
        board.getGrid().rebuild(board.getBalls());
        CountDownLatchImpl latch = new CountDownLatchImpl(nTasks);
        for(int i = 0; i < nTasks; i++){
            exec.execute(new ResolveCollisionsTask(board, i, nTasks, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        board.getBalls().forEach(b -> {
            BoardImpl.resolveCollision(b, board.getPlayerBall(), "player");
            BoardImpl.resolveCollision(b, board.getBotBall(), "bot");
        });
        BoardImpl.resolveCollision(board.getBotBall(), board.getPlayerBall(), "");
    }

    @Override
    public void shutdown() {
        exec.shutdown();
    }
}
