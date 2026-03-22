package pcd.sketch03.model;

import java.util.List;

public class Worker extends Thread{
    CollisionMonitor monitor;
    private List<Ball> balls;
    private Ball pb;
    private Ball bb;

    public Worker(List<Ball> balls, Ball playerBall, Ball botBall, CollisionMonitor monitor){
        this.balls = balls;
        this.pb = playerBall;
        this.bb = botBall;
        this.monitor = monitor;
    }

    public void run(){
        int indexToResolve;
        while( (indexToResolve = monitor.getIndex()) >=0 ){
           Ball b1 = balls.get(indexToResolve);
            for (int j = indexToResolve + 1; j < balls.size(); j++) { //to avoid updating twice the same couple, index i is confronted only with indexes j > i
                Ball b2 = balls.get(j);
                Ball.resolveCollision(b1, b2);
            }
            Ball.resolveCollision(b1, pb);
            Ball.resolveCollision(b1, bb);
        }
    }
}
