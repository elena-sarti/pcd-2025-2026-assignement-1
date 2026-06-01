package tasks.model;

import java.util.List;

public interface SpatialGrid {

    void rebuild(List<BallImpl> balls);

    List<BallImpl> getGridCell(int r, int c);

    int getRows();

    int getCols();
}
