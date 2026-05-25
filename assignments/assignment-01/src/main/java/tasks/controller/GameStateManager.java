package tasks.controller;

import tasks.model.BoardImpl;

public interface GameStateManager {
    void checkRules(BoardImpl board, PhysicsEngineImpl physics);

    boolean isGameOver();

    String getEndMessage();

    CountMonitor getMonitor();
}
