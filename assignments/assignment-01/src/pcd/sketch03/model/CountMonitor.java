package pcd.sketch03.model;

import java.util.Objects;

public class CountMonitor {
    private int countPlayer = 0;
    private int countBot = 0;

    public synchronized void inc(String lastToCollide) {
        if (lastToCollide.equals("player")) {
            countPlayer++;
        } else {
            countBot++;
        }
    }

    public synchronized int getPlayerScore() {
        return countPlayer;
    }

    public synchronized int getBotScore() {
        return countBot;
    }
}
