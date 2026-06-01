package threads.controller;

import threads.model.BoardImpl;
import threads.model.CountMonitor;
import threads.model.PhysicsEngineImpl;

public interface GameStateManager {

    void checkRules(BoardImpl board, PhysicsEngineImpl physics);

    boolean isGameOver();

    String getEndMessage();

    CountMonitor getCountMonitor();
}
