package pcd.sketch03.model;

import javax.management.monitor.CounterMonitor;
import java.util.List;

public class CollisionWorker extends Thread{

    CollisionMonitor collisionMonitor;
    CountMonitor counterMonitor;
    private List<Ball> balls;
    private Ball pb;
    private Ball bb;
    private List<Hole> holes;
    private Board board;

    public CollisionWorker(Board board, CollisionMonitor collisionMonitor, CountMonitor counterMonitor){
        this.board = board;
        balls = board.getBalls();
        pb = board.getPlayerBall();
        bb = board.getBotBall();
        holes = board.getHoles();
        this.collisionMonitor = collisionMonitor;
        this.counterMonitor = counterMonitor;
    }

    public void run(){
        int index;

        while( (index = collisionMonitor.getIndex()) >=0 ){
           Ball b1 = balls.get(index);
            boolean fellInHole = false;

            if (b1.isInHole()){
                continue; // Salta al prossimo indice del monitor
            }

            for (int j = index + 1; j < balls.size(); j++) { //to avoid updating twice the same couple, index i is confronted only with indexes j > i
                Ball b2 = balls.get(j);
                if (b2.isInHole()) continue;
                Ball.resolveCollision(b1, b2, "");

                if (b1.checkInHole(holes)) {
                    handleHole(b1);
                    fellInHole = true;
                    break;
                }
            }

            if (fellInHole) {
                continue;
            }

            Ball.resolveCollision(b1, pb, "player");
            if (b1.checkInHole(holes)) {
                handleHole(b1);
                continue;
            }

            Ball.resolveCollision(b1, bb, "bot");
            if (b1.checkInHole(holes)) {
                handleHole(b1);
            }
        }
    }

    private void handleHole(Ball b) {
        b.setInHole(true);
        counterMonitor.inc(b.getLastToCollide());
    }
}
