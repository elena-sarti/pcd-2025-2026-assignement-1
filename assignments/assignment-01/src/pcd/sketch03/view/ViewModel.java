package pcd.sketch03.view;

import pcd.sketch03.model.*;

import java.util.ArrayList;
import java.util.List;

public class ViewModel {

	private ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
    private BallViewInfo bot;
    private ArrayList<BallViewInfo> holes;
	private int framePerSec;
    private int playerScore = 0;
    private int botScore = 0;
    private volatile boolean gameOver = false;
    private String endMessage = "";
	
	public ViewModel() {
		balls = new ArrayList<>();
        holes = new ArrayList<>();
		framePerSec = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
        this.playerScore = board.getCountMonitor().getPlayerScore();
        this.botScore = board.getCountMonitor().getBotScore();
        this.gameOver = board.isGameOver();
        this.endMessage = board.getEndMessage();
        this.framePerSec = framePerSec;

        if(holes.isEmpty()){
            for (Hole h: board.getHoles()){
                holes.add(new BallViewInfo(h.getPos(), h.getRadius()));
            }
        }

        List<Ball> boardBalls = board.getBalls();
        while (balls.size() < boardBalls.size()) {
            balls.add(new BallViewInfo(new P2d(0,0), 0));
        }
        for (int i = 0; i < boardBalls.size(); i++) {
            Ball b = boardBalls.get(i);
            BallViewInfo info = balls.get(i);
            info.updateData(b.getPos(), b.getRadius());
        }

		var p = board.getPlayerBall();
        if (player == null){
            player = new BallViewInfo(new P2d(0,0), 0);
        }
		player.updateData(p.getPos(), p.getRadius());

        var b = board.getBotBall();
        if (bot == null) {
            bot = new BallViewInfo(new P2d(0,0), 0);
        }
        bot.updateData(b.getPos(), b.getRadius());
	}
	
	public synchronized ArrayList<BallViewInfo> getBalls(){
		return balls;
	}

    public synchronized ArrayList<BallViewInfo> getHoles(){
        return holes;
    }

	public synchronized int getFramePerSec() {
		return framePerSec;
	}

	public synchronized BallViewInfo getPlayerBall() {
		return player;
	}

    public synchronized BallViewInfo getBotBall(){
        return bot;
    }

    public synchronized int getPlayerScore(){
        return playerScore;
    }

    public synchronized int getBotScore(){
        return botScore;
    }

    public synchronized boolean isGameOver(){
        return gameOver;
    }

    public synchronized String getEndMessage(){
        return endMessage;
    }
}
