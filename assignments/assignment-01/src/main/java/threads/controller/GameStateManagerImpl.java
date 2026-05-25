package threads.controller;

import threads.model.*;


public class GameStateManagerImpl implements GameStateManager {
    private boolean gameOver = false;
    private String endMessage = "";
    private CountMonitor monitor = new CountMonitor();

    @Override
    public void checkRules(BoardImpl board, PhysicsEngineImpl physics) {
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

    private void setEnd(String msg, PhysicsEngineImpl physics) {
        this.gameOver = true;
        this.endMessage = msg;
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public String getEndMessage() {
        return endMessage;
    }

    @Override
    public CountMonitor getCountMonitor() {
        return monitor;
    }
}
