package threads;

import threads.controller.BoundedBufferImpl;
import threads.controller.GameLoop;
import threads.controller.InputReaderImpl;
import threads.model.*;
import threads.view.*;

public class PooolGame {

    public static void main(String[] args){

        //var boardConf = new MinimalBoardConf();
        //var boardConf = new LargeBoardConf();
        var boardConf = new MassiveBoardConf();
        BoardImpl board = new BoardImpl();
        board.init(boardConf);
        var buffer = new BoundedBufferImpl<Integer>(5);
        ViewModelImpl viewModel = new ViewModelImpl();
        View view = new View(viewModel, 1200, 800, buffer);
        var gameLoop = new GameLoop(viewModel, board, view);
        var inputReader = new InputReaderImpl(board, buffer);
        gameLoop.start();
        inputReader.start();
    }
}
