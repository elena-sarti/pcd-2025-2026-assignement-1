package threads.model;

import java.util.ArrayList;
import java.util.List;

public class CollisionWorker extends Thread {

    private final CollisionMonitor collisionMonitor;
    private final BoardImpl board;
    private final int id;
    private final int nThreads;
    private final List<Hole> holes;

    public CollisionWorker(BoardImpl board, CollisionMonitor collisionMonitor, int id, int nThreads) {
        this.board = board;
        this.collisionMonitor = collisionMonitor;
        this.id = id;
        this.nThreads = nThreads;
        this.holes = board.getHoles();
    }

    public void run() {
        int totalRows = board.getGrid().getRows();
        int rowsPerThread = totalRows / nThreads;
        int remainder = totalRows % nThreads;
        int startRow = id * rowsPerThread + Math.min(id, remainder);
        int endRow = startRow + rowsPerThread + (id < remainder ? 1 : 0);
        while (!Thread.currentThread().isInterrupted()) {
            collisionMonitor.waitForOrder();
            resolveCollisionInMySlice(startRow, endRow);
            collisionMonitor.notifyWorkDone();
        }
    }

    private void resolveCollisionInMySlice(int startRow, int endRow) {
        for (int r = startRow; r < endRow; r++) {
            for (int c = 0; c < board.getGrid().getCols(); c++) {
                List<BallImpl> currentCell = board.getGrid().getGridCell(r, c);
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
        // the check is made with the current cell and the 8 that are beside it
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                List<BallImpl> cell = board.getGrid().getGridCell(r + dr, c + dc);
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
                            BoardImpl.resolveCollision(b1, b2, "");
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