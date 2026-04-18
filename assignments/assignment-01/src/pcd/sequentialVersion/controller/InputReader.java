package pcd.sequentialVersion.controller;

import pcd.sequentialVersion.model.*;

import static java.awt.event.KeyEvent.*;

public class InputReader extends Thread{
    BoundedBufferImpl<Integer> buffer;
    private int cmd;
    Board board;

    public InputReader(Board board, BoundedBufferImpl<Integer> buffer){
        this.buffer = buffer;
        this.board = board;
    }

    public void run(){
        while(true){
            try {
                cmd = buffer.get();
                resolveCmd(this.board.getPlayerBall(), cmd);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void resolveCmd(Ball playerBall, int cmd){
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
