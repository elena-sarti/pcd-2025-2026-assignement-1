package tasks.controller;

import tasks.model.*;

import java.util.ArrayList;
import java.util.List;

public class ResolveCollisionsTask implements ResolveCollisions {

    private final int id;
    private final int nTasks;
    private final SpatialGridImpl spatialGrid;
    private final BallImpl pb;
    private final BallImpl bb;
    private final List<Hole> holes;
    private final CountDownLatchImpl latch;

    public ResolveCollisionsTask(BoardImpl board, int id, int nTasks, CountDownLatchImpl latch) {
        this.pb = board.getPlayerBall();
        this.bb = board.getBotBall();
        this.holes = board.getHoles();
        this.spatialGrid = board.getGrid();
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
                List<BallImpl> currentCell = spatialGrid.getGridCell(r, c);
                List<BallImpl> cellSnapshot;
                //need to have a copy of the currentCell to do operations on, otherwise other threads could access it at the same time => race conditions
                synchronized(currentCell){
                    cellSnapshot = new ArrayList<>(currentCell);
                }
                for (BallImpl b1 : cellSnapshot) {
                    if (b1 == null || b1.isInHole()) continue;
                    checkLocalCollisions(b1, r, c);
                    checkAndHandleHole(b1);
                }
            }
        }
    }

    private void checkLocalCollisions(BallImpl b1, int r, int c) {
        // the check is made with the current cell and the 8 that are beside it.
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                List<BallImpl> cell = spatialGrid.getGridCell(r + dr, c + dc);
                List<BallImpl> snapshot;
                synchronized(cell){
                    snapshot = new ArrayList<>(cell);
                }
                for (BallImpl b2 : snapshot) {
                    if (b1 == null) break;
                    if (b2 == null || b1 == b2 || b2.isInHole()) continue;
                    // to avoid deadlock, we need to determine the order of the lock, based on the hashcode
                    BallImpl first = b1.hashCode() < b2.hashCode() ? b1 : b2;
                    BallImpl second = (first == b1) ? b2 : b1;
                    synchronized(first){
                        synchronized(second) {
                            b1.resolveCollision(b1, b2, "");
                        }
                    }
                    if (checkAndHandleHole(b1)) return;
                }
            }
        }
    }

    private boolean checkAndHandleHole(BallImpl b) {
        if (b.checkInHole(holes)) {
            b.setInHole(true);
            return true;
        }
        return false;
    }
}