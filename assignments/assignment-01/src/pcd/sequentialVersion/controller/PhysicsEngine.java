package pcd.sequentialVersion.controller;

import pcd.sequentialVersion.model.Ball;
import pcd.sequentialVersion.model.Board;


public class PhysicsEngine {

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
        var balls = board.getBalls();
        for (int i = 0; i < balls.size() - 1; i++) { //controllo se ho collisione con le altre palline. Fatto una sola volta per coppia
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j), ""); //per ogni coppia di palline si devono risolvere collisioni se ci sono. PUNTO IMPORTANTE
            }
            Ball.resolveCollision(balls.get(i), board.getPlayerBall(), "player");
            Ball.resolveCollision(balls.get(i), board.getBotBall(), "bot");
        }
        Ball.resolveCollision(board.getBotBall(), board.getPlayerBall(), "");
        stateManager.checkRules(board, this);
    }
}
