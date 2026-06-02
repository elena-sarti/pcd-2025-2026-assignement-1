package threads;

import threads.controller.BoundedBufferImpl;
import threads.controller.GameLoop;
import threads.controller.InputReader;
import threads.model.*;
import threads.view.*;

public class PooolGame {

    public static void main(String[] args){

        //var boardConf = new MinimalBoardConf();
        //var boardConf = new LargeBoardConf();
        var boardConf = new MassiveBoardConf();
        BoardImpl board = new BoardImpl();
        board.init(boardConf);
        BoundedBufferImpl<Integer> buffer = new BoundedBufferImpl<Integer>(5);
        ViewModelImpl viewModel = new ViewModelImpl();
        ViewImpl view = new ViewImpl(viewModel, 1200, 800, buffer);
        GameLoop gameLoop = new GameLoop(viewModel, board, view);
        InputReader inputReader = new InputReader(board, buffer);
        gameLoop.start();
        inputReader.start();
    }
}
