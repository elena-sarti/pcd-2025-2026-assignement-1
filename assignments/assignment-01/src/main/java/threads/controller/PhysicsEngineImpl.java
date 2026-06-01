package threads.controller;

import threads.model.*;

import java.util.ArrayList;
import java.util.List;


public class PhysicsEngineImpl implements PhysicsEngine {

    private final int nThreads = Runtime.getRuntime().availableProcessors() + 1;
    private final CollisionMonitor collisionMonitor = new CollisionMonitor(nThreads);

    public PhysicsEngineImpl(BoardImpl board, GameStateManagerImpl stateManager) {
        List<CollisionWorker> collisionWorkers = new ArrayList<>();
        for(int i = 0; i < nThreads; i++){
            collisionWorkers.add(new CollisionWorker(board, collisionMonitor, stateManager.getCountMonitor(), i, nThreads));
        }
        for(CollisionWorker worker: collisionWorkers){
            worker.start();
        }
    }

    @Override
    public void update(BoardImpl board, GameStateManagerImpl stateManager, long dt) {
        if (stateManager.isGameOver()) {
            return;
        }
        board.getPlayerBall().updateState(dt, board);
        board.getBotBall().updateState(dt, board);
        board.getBalls().forEach(b -> b.updateState(dt, board));
        board.getGrid().rebuild(board.getBalls());
        collisionMonitor.startResolvingCollisions();
        collisionMonitor.waitForCollisionsToBeResolved();
        board.getPlayerBall().resolveCollision(board.getBotBall(), board.getPlayerBall(), "");
        stateManager.checkRules(board, this);
    }
}
