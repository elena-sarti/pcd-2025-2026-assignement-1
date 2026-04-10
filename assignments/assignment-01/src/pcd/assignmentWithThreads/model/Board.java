package pcd.assignmentWithThreads.model;

import pcd.assignmentWithThreads.model.*;
import java.util.List;

public class Board {

    private List<Ball> balls;
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    private List<Hole> holes;
    private SpatialGrid grid;

    public Board(){}

    public void init(BoardConf conf) {
        this.balls = conf.getSmallBalls();
        this.playerBall = conf.getPlayerBall();
        this.botBall = conf.getBotBall();
        this.bounds = conf.getBoardBoundary();
        this.holes = conf.getHoles();
        this.grid = new SpatialGrid(20, 20, bounds);
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public Ball getPlayerBall() {
        return playerBall;
    }

    public Ball getBotBall() {
        return botBall;
    }

    public List<Hole> getHoles() {
        return holes;
    }

    public Boundary getBounds() {
        return bounds;
    }

    public SpatialGrid getGrid() {
        return grid;
    }

}
