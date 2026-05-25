package tasks.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpatialGridImpl implements SpatialGrid {
    private final int rows, cols;
    private final ArrayList<BallImpl>[][] grid;
    private final Boundary bounds;

    public SpatialGridImpl(int rows, int cols, Boundary bounds) {
        this.rows = rows;
        this.cols = cols;
        this.bounds = bounds;
        this.grid = new ArrayList[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = new ArrayList<>();
    }

    @Override
    public void rebuild(List<BallImpl> balls) {
        clear();
        double rangeY = bounds.y1() - bounds.y0();
        double rangeX = bounds.x1() - bounds.x0();
        for (BallImpl b : balls) {
            if (b.isInHole()) continue;
            int r = Math.max(0, Math.min((int)(((bounds.y1() - b.getPos().y()) / rangeY) * rows), rows - 1));
            int c = Math.max(0, Math.min((int)(((b.getPos().x() - bounds.x0()) / rangeX) * cols), cols - 1));
            synchronized(grid[r][c]) {
                grid[r][c].add(b);
            }
        }
    }

    private void clear() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                synchronized(grid[r][c]) {
                grid[r][c].clear();
            }
    }

    @Override
    public List<BallImpl> getGridCell(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return Collections.emptyList();
        return grid[r][c];
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return cols;
    }
}
