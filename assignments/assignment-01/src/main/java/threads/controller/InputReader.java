package threads.controller;

import threads.model.BoardImpl;
import threads.model.BallImpl;
import threads.model.V2d;

import static java.awt.event.KeyEvent.*;

public class InputReader extends Thread {

    BoundedBufferImpl<Integer> buffer;
    BoardImpl board;

    public InputReader(BoardImpl board, BoundedBufferImpl<Integer> buffer){
        this.buffer = buffer;
        this.board = board;
    }

    @Override
    public void run(){
        while(!Thread.currentThread().isInterrupted()){
            try {
                int cmd = buffer.get();
                resolveCmd(this.board.getPlayerBall(), cmd);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void resolveCmd(BallImpl playerBall, int cmd){
        V2d delta = switch(cmd){
            case VK_UP    -> new V2d(0,  0.1);
            case VK_DOWN  -> new V2d(0, -0.1);
            case VK_LEFT  -> new V2d(-0.1, 0);
            case VK_RIGHT -> new V2d(0.1,  0);
            default -> null;
        };
        if (delta != null) playerBall.applyImpulse(delta);
    }
}
