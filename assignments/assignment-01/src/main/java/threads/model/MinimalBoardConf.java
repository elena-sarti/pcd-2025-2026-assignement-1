package threads.model;


import java.util.ArrayList;
import java.util.List;

public class MinimalBoardConf implements BoardConf {

	@Override
	public BallImpl getPlayerBall() {
    	return new BallImpl(new P2d(-0.5, 0), 0.06, 1, new V2d(0,0.5));
	}

    @Override
    public BallImpl getBotBall() {
        return new BallImpl(new P2d(-0.5, 0), 0.06, 1, new V2d(0,0.5));
    }

	@Override
	public List<BallImpl> getSmallBalls() {
        var balls = new ArrayList<BallImpl>();
    	var b1 = new BallImpl(new P2d(0, 0.5), 0.05, 0.75, new V2d(0,0));
    	var b2 = new BallImpl(new P2d(0.05, 0.55), 0.025, 0.25, new V2d(0,0));
    	balls.add(b1);
    	balls.add(b2);
    	return balls;
	}

    @Override
    public List<Hole> getHoles(){
        var holes = new ArrayList<Hole>();
        var h1 = new Hole(new P2d(-1.5, 1.0), 0.2);
        var h2 = new Hole(new P2d(1.5, 1.0), 0.2);
        holes.add(h1);
        holes.add(h2);
        return holes;
    }

	@Override
	public Boundary getBoardBoundary() {
        return new Boundary(-1.5,-1.0,1.5,1.0);
	}

}
