package threads.model;

import threads.controller.GameStateManagerImpl;

public interface PhysicsEngine {

    void update(BoardImpl board, GameStateManagerImpl stateManager, long dt);
}
