package threads.controller;

import threads.model.BoardImpl;

public interface GameStateManager {

    void checkRules(BoardImpl board, PhysicsEngineImpl physics);

    boolean isGameOver();

    String getEndMessage();

    CountMonitor getCountMonitor();
}
