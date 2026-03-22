package pcd.sketch03.model;

import pcd.sketch03.view.View;
import pcd.sketch03.view.ViewModel;

import java.util.Random;

public class AutonomousUpdater extends Thread{

    private ViewModel viewModel;
    private Board board;
    private View view;

    public AutonomousUpdater(ViewModel viewModel, Board board, View view){
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

        while(true){
            var pb = board.getPlayerBall();
            var bb = board.getBotBall();

            /* if the player ball is stopped and 5 secs have elapsed, then kick the player ball */
            if (pb.getVel().abs() < 0.05 && System.currentTimeMillis() - lastKickTimePB > 200) {
                lastKickTimePB = kickBall(pb, lastKickTimePB);
            }

            /* if the bot ball is stopped and 5 secs have elapsed, then kick the bot ball */
            if (bb.getVel().abs() < 0.05 && System.currentTimeMillis() - lastKickTimeBB > 200) {
                lastKickTimeBB = kickBall(bb, lastKickTimeBB);
            }

            long elapsed = System.currentTimeMillis() - lastUpdateTime;
            lastUpdateTime = System.currentTimeMillis();
            board.updateState(elapsed);

            nFrames++;
            int framePerSec = 0;
            long dt = (System.currentTimeMillis() - t0);
            if (dt > 0) {
                framePerSec = (int)(nFrames*1000/dt);
            }

            viewModel.update(board, framePerSec);
            view.render();
            waitAbit();
        }
    }

    private static long kickBall(Ball b, long lastKickTime){
        var rand = new Random(2);
        var angle = rand.nextDouble() * Math.PI * 0.25;
        var v = new V2d(Math.cos(angle), Math.sin(angle)).mul(1);
        b.kick(v); //assegno quella velocità alla pallina ferma
        lastKickTime = System.currentTimeMillis();
        return lastKickTime;
    }

    private static void waitAbit(){
        try {
            Thread.sleep(30);
        } catch (Exception ex) {}
    }
}
