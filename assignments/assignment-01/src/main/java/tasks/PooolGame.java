package tasks;

import tasks.view.*;
import tasks.model.*;
import tasks.controller.*;

public class PooolGame {

    public static void main(String[] args){
        //var boardConf = new MinimalBoardConf();
        //var boardConf = new LargeBoardConf();
        var boardConf = new MassiveBoardConf();
        BoardImpl board = new BoardImpl();
        board.init(boardConf);
        BoundedBufferImpl<Integer> buffer = new BoundedBufferImpl<Integer>(10);
        ViewModelImpl viewModel = new ViewModelImpl();
        ViewImpl view = new ViewImpl(viewModel, 1200, 800, buffer);
        GameLoop gameLoop = new GameLoop(viewModel, board, view);
        InputReader inputReader = new InputReader(board, buffer);
        gameLoop.start();
        inputReader.start();
    }
}
