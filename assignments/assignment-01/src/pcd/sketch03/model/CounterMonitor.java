package pcd.sketch03.model;

import java.util.Objects;

public class CounterMonitor {
    private int countPlayer;
    private int countBot;

    public CounterMonitor(int countPlayer, int countBot){
        this.countBot = countBot;
        this.countPlayer = countPlayer;
    }

    public synchronized void inc(String lastToCollide) {
        if (Objects.equals(lastToCollide, "player")) {
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
