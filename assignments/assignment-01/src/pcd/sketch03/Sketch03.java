package pcd.sketch03;

import pcd.sketch03.model.*;
import pcd.sketch03.view.*;

public class Sketch03 {

    public static void main(String[] args){

        //var boardConf = new MinimalBoardConf();
        //var boardConf = new LargeBoardConf();
        var boardConf = new MassiveBoardConf();

        Board board = new Board();
        board.init(boardConf);

        ViewModel viewModel = new ViewModel();
        View view = new View(viewModel, 1200, 800);

        var updater = new AutonomousUpdater(viewModel, board, view);

        updater.start();
    }
}
