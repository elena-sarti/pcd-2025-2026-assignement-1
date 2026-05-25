package tasks.view;

import tasks.controller.GameStateManagerImpl;
import tasks.model.BoardImpl;

import java.util.ArrayList;

public class ViewModelImpl implements ViewModel {

	private ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
    private BallViewInfo bot;
    private ArrayList<BallViewInfo> holes;
	private int framePerSec;
    private int playerScore = 0;
    private int botScore = 0;
    private volatile boolean gameOver = false;
    private String endMessage = "";
	
	public ViewModelImpl() {
		balls = new ArrayList<>();
        holes = new ArrayList<>();
		framePerSec = 0;
	}
	
	@Override
    public synchronized void update(BoardImpl board, GameStateManagerImpl gameStateManager, int framePerSec) {
        this.playerScore = gameStateManager.getMonitor().getPlayerScore();
        this.botScore = gameStateManager.getMonitor().getBotScore();
        this.gameOver = gameStateManager.isGameOver();
        this.endMessage = gameStateManager.getEndMessage();
        holes.clear();
        for (var h: board.getHoles()) {
            holes.add(new BallViewInfo(h.getPos(), h.getRadius()));
        }
		balls.clear();
		for (var b: board.getBalls()) {
			balls.add(new BallViewInfo(b.getPos(), b.getRadius()));
		}
		this.framePerSec = framePerSec;
		var p = board.getPlayerBall();
		player = new BallViewInfo(p.getPos(), p.getRadius());
        var b = board.getBotBall();
        bot = new BallViewInfo(b.getPos(), b.getRadius());
	}
	
	@Override
    public synchronized ArrayList<BallViewInfo> getBalls(){
		var copy = new ArrayList<BallViewInfo>();
		copy.addAll(balls);
		return copy;
	}

    @Override
    public synchronized ArrayList<BallViewInfo> getHoles(){
        var copy = new ArrayList<BallViewInfo>();
        copy.addAll(holes);
        return copy;
    }

	@Override
    public synchronized int getFramePerSec() {
		return framePerSec;
	}

	@Override
    public synchronized BallViewInfo getPlayerBall() {
		return player;
	}

    @Override
    public synchronized BallViewInfo getBotBall(){
        return bot;
    }

    @Override
    public synchronized int getPlayerScore(){
        return playerScore;
    }

    @Override
    public synchronized int getBotScore(){
        return botScore;
    }

    @Override
    public synchronized boolean isGameOver(){
        return gameOver;
    }

    @Override
    public synchronized String getEndMessage(){
        return endMessage;
    }
}
