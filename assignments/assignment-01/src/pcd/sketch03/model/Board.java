package pcd.sketch03.model;

import pcd.sketch03.model.Ball;
import pcd.sketch03.model.BoardConf;
import pcd.sketch03.model.Boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
/*
quando devo aggiornare la board, di dt: aggiorno lo stato delle palle, e delle palline
 */
    private static final int nThreads = Runtime.getRuntime().availableProcessors() + 1;
    private List<Ball> balls;
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    private List<Hole> holes;
    List<Worker> workers;
    CollisionMonitor monitor;

    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        workers = new ArrayList<>();
        monitor = new CollisionMonitor(balls.size());
    }
    
    public void updateState(long dt) {

    	playerBall.updateState(dt, this);

        botBall.updateState(dt, this);
    	
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}

        monitor.reset();

        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            Worker w = new Worker(balls, playerBall, botBall, monitor);
            workers.add(w);
            w.start();
        }

        /*
        need to wait for all collisions to be resolved before resolving the collision within the
         two big balls
         */
        for (Worker w : workers) {
            try {
                w.join();
            } catch (InterruptedException e) {
            }
        }
         Ball.resolveCollision(botBall, playerBall);
    }
    
    public List<Ball> getBalls(){
    	return balls;
    }
    
    public Ball getPlayerBall() {
    	return playerBall;
    }

    public Ball getBotBall(){
        return botBall;
    }
    
    public  Boundary getBounds(){
        return bounds;
    }

    public List<Hole> getHoles(){
        return holes;
    }
}
