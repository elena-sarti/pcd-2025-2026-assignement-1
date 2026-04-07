package pcd.assignmentWithThreads.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Board {

    private final int GRID_ROWS = 20;
    private final int GRID_COLS = 20;
    private static final int nThreads = Runtime.getRuntime().availableProcessors() + 1;
    private List<Ball> balls;
    private Ball playerBall;
    private Ball botBall;
    private Boundary bounds;
    private List<Hole> holes;
    List<CollisionWorker> workers;
    long lastKickTimeBB;
    private volatile boolean gameOver = false;
    private String endMessage = "";
    CollisionMonitor collisionMonitor;
    CountMonitor counterMonitor;
    private List<Ball>[][] spatialGrid;
    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall();
        botBall = conf.getBotBall();
    	bounds = conf.getBoardBoundary();
        holes = conf.getHoles();
        lastKickTimeBB = System.currentTimeMillis();
        spatialGrid = new ArrayList[GRID_ROWS][GRID_COLS];
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                spatialGrid[r][c] = new ArrayList<>();
            }
        }
        workers = new ArrayList<>();
        collisionMonitor = new CollisionMonitor(nThreads);
        counterMonitor = new CountMonitor();
        for (int i = 0; i < nThreads; i++) {
            CollisionWorker w = new CollisionWorker(this, collisionMonitor, counterMonitor, i, nThreads);
            workers.add(w);
            w.start();
        }
    }

    public void updateVel(){
       /* if the bot ball is stopped and 5 secs have elapsed, then kick the bot ball */
        if (shouldKick(botBall, lastKickTimeBB, 200)) {
            lastKickTimeBB = kickBotBall(botBall);
        }
    }

    public void updateState(long dt) {
        if (this.gameOver) {
            return;
        }
        moveAllBalls(dt);
        rebuildGrid();
        collisionMonitor.startResolvingCollisions();
        collisionMonitor.waitForCollisionsToBeResolved();
        synchronized(balls){
            balls.removeIf(b -> {
                if (b.isInHole()) {
                    counterMonitor.inc(b.getLastToCollide());
                    return true;
                }
                return false;
            });
        }
        if (balls.isEmpty()){
            gameOver = true;
            endMessage = counterMonitor.getPlayerScore() > counterMonitor.getBotScore() ? "GAME OVER - YOU WON!" : "GAME OVER - you lost :(" ;
            return;
        }
        Ball.resolveCollision(botBall, playerBall, "");
    }

    public void moveAllBalls(long elapsedTime){
        playerBall.updateState(elapsedTime, this);
        if (playerBall.isInHole()) {
            gameOver = true;
            endMessage = "GAME OVER - you lost :(";
            return;
        }
        botBall.updateState(elapsedTime, this);
        if (botBall.isInHole()) {
            gameOver = true;
            endMessage = "GAME OVER - YOU WON!";
            return;
        }
        for (var b: balls) {
            b.updateState(elapsedTime, this);
        }
    }

    private void rebuildGrid() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                synchronized (spatialGrid[r][c]) {
                    spatialGrid[r][c].clear();
                }
            }
        }
        double rangeY = bounds.y1() - bounds.y0();
        double rangeX = bounds.x1() - bounds.x0();
        for (Ball b : balls) {
            if (b.isInHole()) continue;
            int row = (int) (((bounds.y1() - b.getPos().y()) / rangeY) * GRID_ROWS);
            int col = (int) (((b.getPos().x() - bounds.x0()) / rangeX) * GRID_COLS);
            row = Math.max(0, Math.min(row, GRID_ROWS - 1));
            col = Math.max(0, Math.min(col, GRID_COLS - 1));
            synchronized(spatialGrid[row][col]) {
                spatialGrid[row][col].add(b);
            }
        }
    }

    private boolean shouldKick(Ball b, long lastKick, long interval) {
        return b != null && b.getVel().abs() < 0.05 && (System.currentTimeMillis() - lastKick > interval);
    }

    private static long kickBall(Ball b) {
        var rand = new Random();
        var angle = rand.nextDouble() * Math.PI * 0.25;
        var v = new V2d(Math.cos(angle), Math.sin(angle)).mul(1.5);
        b.kick(v);
        return System.currentTimeMillis();
    }

    private long kickBotBall(Ball b){
        List<Hole> holes = getHoles();
        for (Hole h: holes) {
            double distHole = b.distFromHole(h);
            if (distHole < 1){
                double dx = b.getPos().x() - h.getPos().x();
                double dy = b.getPos().y() - h.getPos().y();
                // a new velocity that keeps the bot far from the hole
                V2d v = new V2d(dx, dy).getNormalized().mul(0.75);
                b.kick(v);
                return System.currentTimeMillis();
            }
        }
        return kickBall(b);
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

    public int getGridRows() {
        return GRID_ROWS;
    }

    public int getGridCols() {
        return GRID_COLS;
    }

    public List<Ball> getGridCell(int r, int c) {
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) {
            return Collections.emptyList();
        }
        return spatialGrid[r][c];
    }

}
