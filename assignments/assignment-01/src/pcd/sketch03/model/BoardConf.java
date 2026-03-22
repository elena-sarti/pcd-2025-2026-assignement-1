package pcd.sketch03.model;

import pcd.sketch03.model.Ball;
import pcd.sketch03.model.Boundary;

import java.util.List;

public interface BoardConf {

	Boundary getBoardBoundary();

    List<Hole> getHoles();
	
	Ball getPlayerBall();

    Ball getBotBall();
	
	List<Ball> getSmallBalls();
}
