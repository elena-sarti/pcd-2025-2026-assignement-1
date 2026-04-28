package pcd.sequential.controller;

public class CountMonitor {
    private int countPlayer = 0;
    private int countBot = 0;

    public synchronized void inc(String lastToCollide) {
        if (lastToCollide.equals("player")) {
            countPlayer++;
        } else if (lastToCollide.equals("bot")) {
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
