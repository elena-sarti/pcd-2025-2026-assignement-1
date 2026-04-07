package pcd.assignmentWithThreads.controller;

import pcd.assignmentWithThreads.model.*;

import java.util.ArrayList;
import java.util.List;


public class PhysicsEngine {

    private List<CollisionWorker> collisionWorkers = new ArrayList<>();
    private int nThreads = Runtime.getRuntime().availableProcessors() + 1;
    private CollisionMonitor collisionMonitor = new CollisionMonitor(nThreads);

    public PhysicsEngine(Board board, GameStateManager stateManager) {
        for(int i = 0; i < nThreads; i++){
            collisionWorkers.add(new CollisionWorker(board, collisionMonitor, stateManager.getCountMonitor(), i, nThreads));
        }
        for(CollisionWorker worker: collisionWorkers){
            worker.start();
        }
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
        collisionMonitor.startResolvingCollisions();
        collisionMonitor.waitForCollisionsToBeResolved();
        Ball.resolveCollision(board.getBotBall(), board.getPlayerBall(), "");
        stateManager.checkRules(board, this);
    }
}
