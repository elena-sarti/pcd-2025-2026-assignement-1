package pcd.sequentialVersion.model;

public class Hole {
    private final P2d pos;
    private final double radius;

    public Hole(P2d pos, double radius){
        this.pos = pos;
        this.radius = radius;
    }

    public P2d getPos(){
        return pos;
    }

    public double getRadius(){
        return radius;
    }
}
