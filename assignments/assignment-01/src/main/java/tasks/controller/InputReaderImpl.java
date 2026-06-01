package tasks.controller;

import tasks.model.*;

import static java.awt.event.KeyEvent.*;

public class InputReaderImpl extends Thread implements InputReader {
    BoundedBufferImpl<Integer> buffer;
    BoardImpl board;

    public InputReaderImpl(BoardImpl board, BoundedBufferImpl<Integer> buffer){
        this.buffer = buffer;
        this.board = board;
    }

    @Override
    public void run(){
        while(true){
            try {
                int cmd = buffer.get();
                resolveCmd(this.board.getPlayerBall(), cmd);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void resolveCmd(BallImpl playerBall, int cmd){
        V2d vel = playerBall.getVel();
        V2d newVel;
        switch(cmd){
            case VK_UP:
                newVel = new V2d(0, 0.1);
                break;
            case VK_DOWN:
                newVel = new V2d(0, -0.1);
                break;
            case VK_LEFT:
                newVel = new V2d(-0.1, 0);
                break;
            case VK_RIGHT:
                newVel = new V2d(0.1, 0);
                break;
            default:
                return;
        }
        playerBall.kick(vel.sum(newVel));

    }
}
