package threads.controller;

import threads.model.BoardImpl;
import threads.model.CollisionMonitor;
import threads.model.CollisionWorker;

import java.util.ArrayList;
import java.util.List;


public class PhysicsEngineImpl implements PhysicsEngine {

    private final int nThreads = Runtime.getRuntime().availableProcessors() + 1;
    private final CollisionMonitor collisionMonitor = new CollisionMonitor(nThreads);

    public PhysicsEngineImpl(BoardImpl board) {
        List<CollisionWorker> collisionWorkers = new ArrayList<>();
        for (int i = 0; i < nThreads; i++){
            collisionWorkers.add(new CollisionWorker(board, collisionMonitor, i, nThreads));
        }
        for (CollisionWorker worker: collisionWorkers){
            worker.start();
        }
    }

    @Override
    public void update(BoardImpl board, long dt) {
        board.getPlayerBall().updateState(dt, board);
        board.getBotBall().updateState(dt, board);
        board.getBalls().forEach(b -> b.updateState(dt, board));
        board.getGrid().rebuild(board.getBalls());
        collisionMonitor.startResolvingCollisions();
        collisionMonitor.waitForCollisionsToBeResolved();
        board.getBalls().forEach(b -> {
            BoardImpl.resolveCollision(b, board.getPlayerBall(), "player");
            BoardImpl.resolveCollision(b, board.getBotBall(), "bot");
        });
        BoardImpl.resolveCollision(board.getBotBall(), board.getPlayerBall(), "");
    }
}
