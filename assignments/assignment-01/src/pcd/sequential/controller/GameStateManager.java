package pcd.sequential.controller;

import pcd.sequential.model.Board;


public class GameStateManager {
    private boolean gameOver = false;
    private String endMessage = "";
    private CountMonitor monitor = new CountMonitor();

    public void checkRules(Board board, PhysicsEngine physics) {
        if (gameOver) return;
        synchronized(board.getBalls()) {
            board.getBalls().removeIf(b -> {
                if (b.isInHole()) {
                    monitor.inc(b.getLastToCollide());
                    return true;
                }
                return false;
            });
        }
        if (board.getPlayerBall().isInHole() || (board.getBalls().isEmpty() && monitor.getBotScore() > monitor.getPlayerScore())) {
            setEnd("GAME OVER - you lost :(", physics);
        } else if (board.getBotBall().isInHole() || board.getBalls().isEmpty()) {
            setEnd("GAME OVER - YOU WON!", physics);
        }
    }

    private void setEnd(String msg, PhysicsEngine physics) {
        this.gameOver = true;
        this.endMessage = msg;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getEndMessage() {
        return endMessage;
    }

    public CountMonitor getCountMonitor() {
        return monitor;
    }
}
