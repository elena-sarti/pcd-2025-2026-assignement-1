package tasks.controller;

import tasks.model.BoardImpl;

public interface PhysicsEngine {

    void update(BoardImpl board, long dt);

    void shutdown();
}
