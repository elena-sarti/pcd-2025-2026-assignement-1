package pcd.sketch01;

import java.util.*;

public class Board {
/*
quando devo aggiornare la board, di dt: aggiorno lo stato delle palle, e delle palline
 */
    private List<Ball> balls;    
    private Ball playerBall;
    private Boundary bounds;
    
    public Board(){} 
    
    public void init(BoardConf conf) {
    	balls = conf.getSmallBalls();    	
    	playerBall = conf.getPlayerBall(); 
    	bounds = conf.getBoardBoundary();
    }
    
    public void updateState(long dt) {

    	playerBall.updateState(dt, this);
    	
    	for (var b: balls) {
    		b.updateState(dt, this);
    	}       	
    	
    	for (int i = 0; i < balls.size() - 1; i++) { //controllo se ho collisione con le altre palline. Fatto una sola volta per coppia
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j)); //per ogni coppia di palline si devono risolvere collisioni se ci sono. PUNTO IMPORTANTE
            }
        }
    	for (var b: balls) {
    		Ball.resolveCollision(playerBall, b); //nell'assignment va fatto per le due palle grandi. Più thread/task per farlo
    	} 
    	   	    	
    }
    
    public List<Ball> getBalls(){
    	return balls;
    }
    
    public Ball getPlayerBall() {
    	return playerBall;
    }
    
    public  Boundary getBounds(){
        return bounds;
    }
}
