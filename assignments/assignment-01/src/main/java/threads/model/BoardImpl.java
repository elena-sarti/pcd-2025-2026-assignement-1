package threads.model;

import java.util.List;

public class BoardImpl implements Board {

    private List<BallImpl> balls;
    private BallImpl playerBall;
    private BallImpl botBall;
    private Boundary bounds;
    private List<Hole> holes;
    private SpatialGridImpl grid;

    public BoardImpl(){}

    @Override
    public void init(BoardConf conf) {
        this.balls = conf.getSmallBalls();
        this.playerBall = conf.getPlayerBall();
        this.botBall = conf.getBotBall();
        this.bounds = conf.getBoardBoundary();
        this.holes = conf.getHoles();
        this.grid = new SpatialGridImpl(60, 60, bounds);
    }

    @Override
    public List<BallImpl> getBalls() {
        return balls;
    }

    @Override
    public BallImpl getPlayerBall() {
        return playerBall;
    }

    @Override
    public BallImpl getBotBall() {
        return botBall;
    }

    @Override
    public List<Hole> getHoles() {
        return holes;
    }

    @Override
    public Boundary getBounds() {
        return bounds;
    }

    @Override
    public SpatialGridImpl getGrid() {
        return grid;
    }

}
