package threads.controller;

import threads.model.BoardImpl;

public interface PhysicsEngine {

    void update(BoardImpl board, long dt);
}
