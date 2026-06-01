package tasks.model;

import tasks.controller.GameStateManagerImpl;

public interface PhysicsEngine {

    void update(BoardImpl board, GameStateManagerImpl stateManager, long dt);

    void shutdown();
}
