package tasks.model;

import java.util.List;

public class BallImpl implements Ball {

    public static final double FRICTION_FACTOR = 0.25; 	/* 0 minimum */
    public static final double RESTITUTION_FACTOR = 1;

    private P2d pos;
    private V2d vel;
    private final double radius;
    private final double mass;
    private volatile boolean inHole = false;
    private String lastToCollide = "";

    public BallImpl(P2d pos, double radius, double mass, V2d vel){
        this.pos = pos;
        this.radius = radius;
        this.mass = mass;
        this.vel = vel;
    }

    @Override
    public synchronized void updateState(long dt, BoardImpl ctx){
        if (this.inHole) {
            this.vel = new V2d(0, 0);
            return;
        }
        double speed = vel.abs();
        double dt_scaled = dt*0.001;
        if (speed > 0.001) {
            double dec    = FRICTION_FACTOR * dt_scaled; //constant deceleration
            double factor = Math.max(0, speed - dec) / speed;
            vel = vel.mul(factor);
        } else {
            vel = new V2d(0,0);
        }
        pos = pos.sum(vel.mul(dt_scaled));
        if (checkInHole(ctx.getHoles())){
            this.setInHole(true);
            this.vel = new V2d(0,0);
            return;
        }
        applyBoundaryConstraints(ctx);
    }

    @Override
    public synchronized void kick(V2d vel) {
        this.vel = vel;
    }

    @Override
    public synchronized void applyImpulse(V2d delta) {
        vel = vel.sum(delta);
    }

    private synchronized void applyBoundaryConstraints(BoardImpl ctx){
        Boundary bounds = ctx.getBounds();
        if (pos.x() + radius > bounds.x1()){
            pos = new P2d(bounds.x1() - radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.x() - radius < bounds.x0()){
            pos = new P2d(bounds.x0() + radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.y() + radius > bounds.y1()){
            pos = new P2d(pos.x(), bounds.y1() - radius);
            vel = vel.getSwappedY();
        } else if (pos.y() - radius < bounds.y0()){
            pos = new P2d(pos.x(), bounds.y0() + radius);
            vel = vel.getSwappedY();
        }
    }

    @Override
    public synchronized boolean checkInHole(List<Hole> holes){
        for (Hole h : holes) {
            // the ball hits the hole if the distance is minor than the sum of the radii
            if (distFromHole(h) < 0) {
                this.setInHole(true);
                this.vel = new V2d(0, 0); // stopping the ball
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized double distFromHole(Hole h){
        double dx = pos.x() - h.pos().x();
        double dy = pos.y() - h.pos().y();
        double distSq = dx*dx + dy*dy; // we use the squares for velocity
        double minFoundDist = Math.pow(h.radius() + this.radius, 2);
        return distSq - minFoundDist;
    }

    @Override
    public synchronized P2d getPos(){
        return pos;
    }

    @Override
    public synchronized void setPos(P2d pos){
        this.pos = pos;
    }

    @Override
    public synchronized double getMass() {
        return mass;
    }

    @Override
    public synchronized V2d getVel() {
        return vel;
    }

    @Override
    public synchronized double getRadius() {
        return radius;
    }

    @Override
    public synchronized void setInHole(boolean b){
        inHole = b;
    }

    @Override
    public synchronized boolean isInHole(){
        return inHole;
    }

    @Override
    public synchronized void setLastToCollide(String last){
        lastToCollide = last;
    }

     @Override
     public synchronized String getLastToCollide(){
        return lastToCollide;
     }
}
