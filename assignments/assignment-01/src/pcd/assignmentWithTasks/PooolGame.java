package pcd.assignmentWithTasks;

import pcd.assignmentWithTasks.view.*;
import pcd.assignmentWithTasks.model.*;
import pcd.assignmentWithTasks.controller.*;

public class PooolGame {

    public static void main(String[] args){

        //var boardConf = new MinimalBoardConf();
        //var boardConf = new LargeBoardConf();
        var boardConf = new MassiveBoardConf();
        Board board = new Board();
        board.init(boardConf);
        var buffer = new BoundedBufferImpl<Integer>(5);
        ViewModel viewModel = new ViewModel();
        View view = new View(viewModel, 1200, 800, buffer);
        var updater = new AutonomousUpdater(viewModel, board, view);
        var inputReader = new InputReader(board, buffer);
        updater.start();
        inputReader.start();
    }
}
