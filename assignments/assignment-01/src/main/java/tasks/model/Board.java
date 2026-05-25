package tasks.model;

import java.util.List;

public interface Board {
    void init(BoardConf conf);

    List<BallImpl> getBalls();

    BallImpl getPlayerBall();

    BallImpl getBotBall();

    List<Hole> getHoles();

    Boundary getBounds();

    SpatialGridImpl getGrid();
}
