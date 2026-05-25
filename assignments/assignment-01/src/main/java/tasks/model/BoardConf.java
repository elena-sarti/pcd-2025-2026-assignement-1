package tasks.model;

import java.util.List;

public interface BoardConf {

	Boundary getBoardBoundary();

    List<Hole> getHoles();
	
	BallImpl getPlayerBall();

    BallImpl getBotBall();
	
	List<BallImpl> getSmallBalls();
}
