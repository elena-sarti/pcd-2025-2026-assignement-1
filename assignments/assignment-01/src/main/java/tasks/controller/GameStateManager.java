package tasks.controller;

import tasks.model.BoardImpl;
import tasks.model.CountMonitor;
import tasks.model.PhysicsEngineImpl;

public interface GameStateManager {

    void checkRules(BoardImpl board, PhysicsEngineImpl physics);

    boolean isGameOver();

    String getEndMessage();

    CountMonitor getMonitor();
}
