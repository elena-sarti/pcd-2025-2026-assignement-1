package pcd.sequentialVersion.model;

import java.util.List;

public interface BoardConf {

	Boundary getBoardBoundary();

    List<Hole> getHoles();
	
	Ball getPlayerBall();

    Ball getBotBall();
	
	List<Ball> getSmallBalls();
}
