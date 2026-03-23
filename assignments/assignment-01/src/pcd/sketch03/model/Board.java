package pcd.sketch03.model;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private static final int nThreads = 20; //Runtime.getRuntime().availableProcessors() + 1;
    private List<Ball> balls;
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    private List<Hole> holes;
    List<CollisionWorker> workers;
    private volatile boolean gameOver = false;
    private String endMessage = "";
    CollisionMonitor collisionMonitor;
    CountMonitor counterMonitor;
    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        workers = new ArrayList<>();
        collisionMonitor = new CollisionMonitor(balls.size());
        counterMonitor = new CountMonitor();
    }
    
    public void updateState(long dt) {
        if (this.gameOver) {
            return;
        }

    	playerBall.updateState(dt, this);
        if (playerBall.isInHole()) {
            gameOver = true;
            endMessage = "GAME OVER - you lost :(";
            return;
        }

        botBall.updateState(dt, this);
        if (botBall.isInHole()) {
            gameOver = true;
            endMessage = "GAME OVER - YOU WON!";
            return;
        }

    	for (var b: balls) {
    		b.updateState(dt, this);
    	}

        collisionMonitor.reset(balls.size());

        List<CollisionWorker> workers = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            CollisionWorker w = new CollisionWorker(this, collisionMonitor, counterMonitor);
            workers.add(w);
            w.start();
        }

        /*
        need to wait for all collisions to be resolved before resolving the collision within the
         two big balls
         */
        for (CollisionWorker w : workers) {
            try {
                w.join();
            } catch (InterruptedException e) {
            }
        }

        balls.removeIf(b -> {
            if (b.isInHole()) {
                counterMonitor.inc(b.getLastToCollide());
                return true;
            }
            return false;
        });

        if ( balls.isEmpty() ){
            gameOver = true;
            endMessage = counterMonitor.getPlayerScore() > counterMonitor.getBotScore() ? "GAME OVER - YOU WON!" : "GAME OVER - you lost :(" ;
            return;
        }

        Ball.resolveCollision(botBall, playerBall, "");

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

    public CountMonitor getCountMonitor(){
        return counterMonitor;
    }

    public boolean isGameOver(){
        return gameOver;
    }

    public String getEndMessage(){
        return endMessage;
    }
}
