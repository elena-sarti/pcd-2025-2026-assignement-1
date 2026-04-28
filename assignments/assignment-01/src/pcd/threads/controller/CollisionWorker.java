package pcd.threads.controller;

import pcd.threads.model.Ball;
import pcd.threads.model.Board;
import pcd.threads.model.Hole;

import java.util.ArrayList;
import java.util.List;

public class CollisionWorker extends Thread {

    private final CollisionMonitor collisionMonitor;
    private final CountMonitor counterMonitor;
    private final Board board;
    private final int id;
    private final int nThreads;
    private Ball pb;
    private Ball bb;
    private List<Hole> holes;

    public CollisionWorker(Board board, CollisionMonitor collisionMonitor, CountMonitor counterMonitor, int id, int nThreads) {
        this.board = board;
        this.collisionMonitor = collisionMonitor;
        this.counterMonitor = counterMonitor;
        this.id = id;
        this.nThreads = nThreads;
        this.pb = board.getPlayerBall();
        this.bb = board.getBotBall();
        this.holes = board.getHoles();
    }

    public void run() {
        while (true) {
            collisionMonitor.waitForOrder();
            resolveCollisionInMySlice();
            collisionMonitor.notifyWorkDone();
        }
    }

    private void resolveCollisionInMySlice() {
        int totalRows = board.getGrid().getRows();
        int rowsPerThread = totalRows / nThreads;
        int remainder = totalRows % nThreads;
        int startRow = id * rowsPerThread + Math.min(id, remainder);
        int endRow = startRow + rowsPerThread + (id < remainder ? 1 : 0);
        for (int r = startRow; r < endRow; r++) {
            for (int c = 0; c < board.getGrid().getCols(); c++) {
                List<Ball> currentCell = board.getGrid().getGridCell(r, c);
                List<Ball> cellSnapshot;
                //need to have a copy of the currentCell to do operations on, otherwise other threads could access it at the same time => race conditions
                synchronized(currentCell){
                    cellSnapshot = new ArrayList<>(currentCell);
                }
                for (Ball b1 : cellSnapshot) {
                    if (b1 == null || b1.isInHole()) continue;
                    checkLocalCollisions(b1, r, c);
                    if (b1.isInHole()) continue;
                    //in order to access the method safely, we need to get the lock on the playerball
                    synchronized(pb) {
                        Ball.resolveCollision(b1, pb, "player");
                    }
                    if (checkAndHandleHole(b1)) continue;
                    //the same applies also to solve the collision with the botball
                    synchronized(bb) {
                        Ball.resolveCollision(b1, bb, "bot");
                    }
                    checkAndHandleHole(b1);
                }
            }
        }
    }

    private void checkLocalCollisions(Ball b1, int r, int c) {
        // the check is made with the current cell and the 8 that are beside it.
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                List<Ball> cell = board.getGrid().getGridCell(r + dr, c + dc);
                List<Ball> snapshot;
                synchronized(cell){
                    snapshot = new ArrayList<>(cell);
                }
                for (Ball b2 : snapshot) {
                    if (b1 == null) break;
                    if (b2 == null || b1 == b2 || b2.isInHole()) continue;
                    // to avoid deadlock, we need to determine the order of the lock, based on the hashcode
                    Ball first = b1.hashCode() < b2.hashCode() ? b1 : b2;
                    Ball second = (first == b1) ? b2 : b1;
                    synchronized(first){
                        synchronized(second) {
                            Ball.resolveCollision(b1, b2, "");
                        }
                    }
                    if (checkAndHandleHole(b1)) return;
                }
            }
        }
    }

    private boolean checkAndHandleHole(Ball b) {
        if (b.checkInHole(holes)) {
            b.setInHole(true);
            counterMonitor.inc(b.getLastToCollide());
            return true;
        }
        return false;
    }
}