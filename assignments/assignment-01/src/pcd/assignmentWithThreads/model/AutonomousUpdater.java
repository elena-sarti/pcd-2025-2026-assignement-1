package pcd.assignmentWithThreads.model;

import pcd.assignmentWithThreads.view.View;
import pcd.assignmentWithThreads.view.ViewModel;

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
        int nFrames = 0;
        while (!board.isGameOver()) {
            long elapsed = System.currentTimeMillis() - lastUpdateTime;
            lastUpdateTime = System.currentTimeMillis();
            board.updateVel();
            board.updateState(elapsed);
            nFrames++;
            int framePerSec = 0;
            long dt = (System.currentTimeMillis() - t0);
            if (dt > 0) {
                framePerSec = (int) (nFrames * 1000 / dt);
            }
            viewModel.update(board, framePerSec);
            view.render();
        }
    }
}
