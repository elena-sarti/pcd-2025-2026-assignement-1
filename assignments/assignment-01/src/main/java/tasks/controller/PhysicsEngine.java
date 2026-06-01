package tasks.controller;

import tasks.model.BoardImpl;

public interface PhysicsEngine {

    void update(BoardImpl board, GameStateManagerImpl stateManager, long dt);

    void shutdown();
}
