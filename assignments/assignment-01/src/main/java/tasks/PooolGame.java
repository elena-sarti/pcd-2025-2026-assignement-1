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
        var buffer = new BoundedBufferImpl<Integer>(10);
        ViewModelImpl viewModel = new ViewModelImpl();
        ViewImpl view = new ViewImpl(viewModel, 1200, 800, buffer);
        var gameLoop = new GameLoop(viewModel, board, view);
        var inputReader = new InputReaderImpl(board, buffer);
        gameLoop.start();
        inputReader.start();
    }
}
