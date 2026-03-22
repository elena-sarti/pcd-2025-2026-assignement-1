package pcd.sketch03.model;

import pcd.sketch03.model.Ball;
import pcd.sketch03.model.BoardConf;
import pcd.sketch03.model.Boundary;

import javax.management.monitor.CounterMonitor;
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
    private int countPlayer = 0;
    private int countBot = 0;
    CollisionMonitor collisionMonitor;
    pcd.sketch03.model.CounterMonitor counterMonitor;


    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        workers = new ArrayList<>();
        collisionMonitor = new CollisionMonitor(balls.size());
        counterMonitor = new pcd.sketch03.model.CounterMonitor(countPlayer, countBot);
    }
    
    public void updateState(long dt) {

    	playerBall.updateState(dt, this);
        botBall.updateState(dt, this);
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}

        collisionMonitor.reset(balls.size());

        List<Worker> workers = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            Worker w = new Worker(this, collisionMonitor, counterMonitor);
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
        //debug
//        if (!balls.isEmpty()) {
//            balls.get(0).setInHole(true);
//        }
        balls.removeIf(b -> {
            if (b.getInHole()) {
                counterMonitor.inc(b.getLastToCollide());
                return true;
            }
            return false;
        });
        System.out.println("Palline rimaste: " + balls.size());

        Ball.resolveCollision(botBall, playerBall, -1, "");

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
