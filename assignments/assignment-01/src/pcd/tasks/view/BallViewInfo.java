package pcd.tasks.view;

import pcd.tasks.model.P2d;

public class BallViewInfo {
    private P2d pos;
    private double radius;

    public BallViewInfo(P2d pos, double radius) {
        this.pos = pos;
        this.radius = radius;
    }

    public void updateData(P2d pos, double radius) {
        this.pos = pos;
        this.radius = radius;
    }

    public P2d pos() { return pos; }
    public double radius() { return radius; }
}
