package pcd.assignmentWithTasks.controller;

import pcd.assignmentWithTasks.model.*;

import java.util.List;
import java.util.Random;

public class BotController {
    private long lastKickTime = System.currentTimeMillis();

    public void update(Board board) {
        Ball bot = board.getBotBall();
        if (bot != null && bot.getVel().abs() < 0.05 && (System.currentTimeMillis() - lastKickTime > 200)) {
            lastKickTime = kickBotBall(bot, board.getHoles());
        }
    }

    private long kickBotBall(Ball b, List<Hole> holes) {
        for (Hole h : holes) {
            if (b.distFromHole(h) < 1) {
                V2d v = new V2d(b.getPos().x() - h.getPos().x(), b.getPos().y() - h.getPos().y())
                        .getNormalized().mul(0.75);
                b.kick(v);
                return System.currentTimeMillis();
            }
        }
        return kickGeneric(b);
    }

    private long kickGeneric(Ball b) {
        var rand = new Random();
        var v = new V2d(Math.cos(rand.nextDouble()*Math.PI*0.25), Math.sin(rand.nextDouble()*Math.PI*0.25)).mul(1.5);
        b.kick(v);
        return System.currentTimeMillis();
    }
}
