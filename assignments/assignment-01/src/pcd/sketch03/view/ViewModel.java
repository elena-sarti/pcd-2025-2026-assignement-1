package pcd.sketch03.view;

import pcd.sketch03.model.*;

import java.util.ArrayList;

record BallViewInfo(P2d pos, double radius) {}

public class ViewModel {

	private ArrayList<BallViewInfo> balls;
	private BallViewInfo player;
    private BallViewInfo bot;
    private ArrayList<BallViewInfo> holes;
	private int framePerSec;
	
	public ViewModel() {
		balls = new ArrayList<BallViewInfo>();
        holes = new ArrayList<>();
		framePerSec = 0;
	}
	
	public synchronized void update(Board board, int framePerSec) {
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
	
	public synchronized ArrayList<BallViewInfo> getBalls(){
		var copy = new ArrayList<BallViewInfo>();
		copy.addAll(balls);
		return copy;
	}

    public synchronized ArrayList<BallViewInfo> getHoles(){
        var copy = new ArrayList<BallViewInfo>();
        copy.addAll(holes);
        return copy;
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

}
