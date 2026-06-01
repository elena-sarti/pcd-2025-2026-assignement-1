package tasks.view;

import tasks.controller.GameStateManagerImpl;
import tasks.model.BoardImpl;

import java.util.ArrayList;

public interface ViewModel {

    void update(BoardImpl board, GameStateManagerImpl gameStateManager, int framePerSec);

    ArrayList<BallViewInfo> getBalls();

    ArrayList<BallViewInfo> getHoles();

    int getFramePerSec();

    BallViewInfo getPlayerBall();

    BallViewInfo getBotBall();

    int getPlayerScore();

    int getBotScore();

    boolean isGameOver();

    String getEndMessage();
}
