package pcd.sketch03.model;

import javax.management.monitor.CounterMonitor;
import java.util.List;

public class Worker extends Thread{

    CollisionMonitor collisionMonitor;
    pcd.sketch03.model.CounterMonitor counterMonitor;
    private List<Ball> balls;
    private Ball pb;
    private Ball bb;
    private Boundary boundary;
    private Board board;

    public Worker(Board board, CollisionMonitor collisionMonitor, pcd.sketch03.model.CounterMonitor counterMonitor){
        this.board = board;
        balls = board.getBalls();
        pb = board.getPlayerBall();
        bb = board.getBotBall();
        boundary = board.getBounds();
        this.collisionMonitor = collisionMonitor;
        this.counterMonitor = counterMonitor;
    }

    public void run(){
        int indexToResolve;

        while( (indexToResolve = collisionMonitor.getIndex()) >=0 ){
           Ball b1 = balls.get(indexToResolve);
            boolean fellInHole = false;

            if (b1.getInHole()){
                continue; // Salta al prossimo indice del monitor
            }

            for (int j = indexToResolve + 1; j < balls.size(); j++) { //to avoid updating twice the same couple, index i is confronted only with indexes j > i
                Ball b2 = balls.get(j);
                if (b2.getInHole()) continue;
                Ball.resolveCollision(b1, b2, -1, "");

                if (isInHole(b1, boundary)) {
                    handleHole(b1);
                    fellInHole = true;
                    break;
                }
            }

            if (fellInHole) {
                continue;
            }

            Ball.resolveCollision(b1, pb, indexToResolve, "player");
            if (isInHole(b1, boundary)) {
                handleHole(b1);
                continue;
            }

            Ball.resolveCollision(b1, bb, indexToResolve, "bot");
            if (isInHole(b1, boundary)) {
                handleHole(b1);
            }
        }
    }

    private boolean isInHole(Ball b, Boundary boundary){
        double px = b.getPos().x();
        double py = b.getPos().y();
        double r = b.getRadius();

        // 1. Distanza dall'angolo in alto a sinistra (x0, y0)
        double distTopLeft = Math.hypot(px - boundary.x0(), py - boundary.y0());

        // 2. Distanza dall'angolo in alto a destra (x1, y0)
        double distTopRight = Math.hypot(px - boundary.x1(), py - boundary.y0());

        // Una pallina è in buca se il suo centro è abbastanza vicino all'angolo.
        // Usiamo r * 1.5 o r * 2 per rendere la "buca" un po' più accogliente,
        // altrimenti deve essere perfettamente centrata per cadere.
        double holeTolerance = r*2;

        return distTopLeft < holeTolerance || distTopRight < holeTolerance;
    }

    private void handleHole(Ball b) {
        b.setInHole(true);
        counterMonitor.inc(b.getLastToCollide());
    }
}
