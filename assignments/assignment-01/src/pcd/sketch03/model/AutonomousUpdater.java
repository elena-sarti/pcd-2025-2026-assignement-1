package pcd.sketch03.model;

import pcd.sketch03.view.View;
import pcd.sketch03.view.ViewModel;

import java.util.List;
import java.util.Random;

public class AutonomousUpdater extends Thread {

    private ViewModel viewModel;
    private Board board;
    private View view;

    public AutonomousUpdater(ViewModel viewModel, Board board, View view) {
        this.viewModel = viewModel;
        this.board = board;
        this.view = view;
    }

    public void run() {
        long t0 = System.currentTimeMillis();
        long lastUpdateTime = System.currentTimeMillis();
        long lastKickTimePB = System.currentTimeMillis();
        long lastKickTimeBB = System.currentTimeMillis();
        int nFrames = 0;

        while (!board.isGameOver()) {
            var pb = board.getPlayerBall();
            var bb = board.getBotBall();

            /* if the player ball is stopped and 5 secs have elapsed, then kick the player ball */
            if (shouldKick(pb, lastKickTimePB, 500)) {
                lastKickTimePB = kickBall(pb);
            }

            /* if the bot ball is stopped and 5 secs have elapsed, then kick the bot ball */
            if (shouldKick(bb, lastKickTimeBB, 200)) {
                lastKickTimeBB = kickBotBall(bb);
            }

            long elapsed = System.currentTimeMillis() - lastUpdateTime;
            lastUpdateTime = System.currentTimeMillis();
            board.updateState(elapsed);

            nFrames++;
            int framePerSec = 0;
            long dt = (System.currentTimeMillis() - t0);
            if (dt > 0) {
                framePerSec = (int) (nFrames * 1000 / dt);
            }

            viewModel.update(board, framePerSec);
            view.render();
            waitAbit();
        }
    }

    private boolean shouldKick(Ball b, long lastKick, long interval) {
        return b != null && b.getVel().abs() < 0.05 && (System.currentTimeMillis() - lastKick > interval);
    }

    private static long kickBall(Ball b) {
        var rand = new Random();
        var angle = rand.nextDouble() * Math.PI * 0.25;
        var v = new V2d(Math.cos(angle), Math.sin(angle)).mul(1.5);
        b.kick(v); //assegno quella velocità alla pallina ferma
        return System.currentTimeMillis();
    }

    private long kickBotBall(Ball b){
        List<Hole> holes = board.getHoles();
        for (Hole h: holes) {
            double distHole = b.distFromHole(h);
            if (distHole < 1){
                double dx = b.getPos().x() - h.getPos().x();
                double dy = b.getPos().y() - h.getPos().y();
                // a new velocity that keeps the bot far from the hole
                V2d v = new V2d(dx, dy).getNormalized().mul(1.5);
                b.kick(v);
                return System.currentTimeMillis();
            }
        }
        return kickBall(b);
    }

    private static void waitAbit() {
        try {
            Thread.sleep(20);
        } catch (Exception ex) {
        }
    }
}
