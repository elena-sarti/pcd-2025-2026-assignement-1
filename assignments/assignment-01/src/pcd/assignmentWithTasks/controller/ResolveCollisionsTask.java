package pcd.assignmentWithTasks.controller;

import pcd.assignmentWithTasks.model.*;

import java.util.ArrayList;
import java.util.List;

public class ResolveCollisionsTask implements Runnable {

    private final int id;
    private final int nTasks;
    private SpatialGrid spatialGrid;
    private Ball pb;
    private Ball bb;
    private List<Hole> holes;
    private CountDownLatch latch;
    private CountMonitor counterMonitor;

    public ResolveCollisionsTask(Board board, CountMonitor counterMonitor, int id, int nTasks, CountDownLatch latch) {
        this.pb = board.getPlayerBall();
        this.bb = board.getBotBall();
        this.holes = board.getHoles();
        this.spatialGrid = board.getGrid();
        this.counterMonitor = counterMonitor;
        this.id = id;
        this.nTasks = nTasks;
        this.latch = latch;
    }

    public void run() {
        try {
            resolveCollisionInMySlice();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    private void resolveCollisionInMySlice() {
        int totalRows = spatialGrid.getRows();
        int rowsPerThread = totalRows / nTasks;
        int remainder = totalRows % nTasks;
        int startRow = id * rowsPerThread + Math.min(id, remainder);
        int endRow = startRow + rowsPerThread + (id < remainder ? 1 : 0);
        for (int r = startRow; r < endRow; r++) {
            for (int c = 0; c < spatialGrid.getCols(); c++) {
                List<Ball> currentCell = spatialGrid.getGridCell(r, c);
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
                List<Ball> cell = spatialGrid.getGridCell(r + dr, c + dc);
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
            return true;
        }
        return false;
    }
}