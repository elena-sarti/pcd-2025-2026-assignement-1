package tasks.model;


import java.util.List;

public class BallImpl implements Ball {

    private P2d pos;
    private V2d vel;
    private double radius;
    private double mass;
    private volatile boolean inHole = false;
    private String lastToCollide = "";

    private static double FRICTION_FACTOR = 0.25; 	/* 0 minimum */
    private static double RESTITUTION_FACTOR = 1;


    public BallImpl(P2d pos, double radius, double mass, V2d vel){
        this.pos = pos;
        this.radius = radius;
        this.mass = mass;
        this.vel = vel;
    }

    @Override
    public void updateState(long dt, BoardImpl ctx){
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
    public void resolveCollision(BallImpl a, BallImpl b, String ballType) {
        if (a.isInHole()) return;
        /* check if there is a collision */
        double dx = b.pos.x() - a.pos.x();
        double dy = b.pos.y() - a.pos.y();
        double dist = Math.hypot(dx, dy);
        double minD = a.radius + b.radius;
        /* compute dv = b.pos - a.pos vector */
        if (dist < minD && dist > 1e-6) {
            BallImpl.setCollider(a, b, ballType);
            /*
             * Collision case - what to do:
             * 1) solve overlaps, moving balls
             * 2) update velocities
             */
            double nx = dx / dist;
            double ny = dy / dist;
            /*
             * Update positions to solve overlaps, moving balls along dvn
             * - the displacements is proportional to the mass
             */
            double overlap = minD - dist;
            double totalM = a.mass + b.mass;
            double a_factor = overlap * (b.mass / totalM);
            double a_deltax = nx * a_factor;
            double a_deltay = ny * a_factor;
            a.pos = new P2d(a.getPos().x() - a_deltax, a.getPos().y() - a_deltay);
            double b_factor = overlap * (a.mass / totalM);
            double b_deltax = nx * a_factor;
            double b_deltay = ny * a_factor;
            b.pos = new P2d(b.getPos().x() + b_deltax, b.getPos().y() + b_deltay);
            /* Update velocities */
            /* relative speed along the normal vector*/
            double dvx = b.vel.x() - a.vel.x();
            double dvy = b.vel.y() - a.vel.y();
            double dvn = dvx * nx + dvy * ny;
            if (dvn <= 0) { /* if not already separating, update velocities */
                double imp = -(1 + BallImpl.RESTITUTION_FACTOR) * dvn / (1.0 / a.getMass() + 1.0 / b.getMass());
                a.vel = new V2d(a.vel.x() - (imp / a.mass) * nx, a.vel.y() - (imp / a.mass) * ny);
                b.vel = new V2d(b.vel.x() + (imp / b.mass) * nx, b.vel.y() + (imp / b.mass) * ny);
            }
        }
    }

    @Override
    public synchronized void kick(V2d vel) {
        this.vel = vel;
    }

    /**
     *
     * Keep the ball inside the boundaries, updating the velocity in the case of bounces
     *
     * @param ctx
     */
    private void applyBoundaryConstraints(BoardImpl ctx){
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
    public boolean checkInHole(List<Hole> holes){
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
    public double distFromHole(Hole h){
        double dx = pos.x() - h.getPos().x();
        double dy = pos.y() - h.getPos().y();
        double distSq = dx*dx + dy*dy; // we use the squares for velocity
        double minFoundDist = Math.pow(h.getRadius() + this.radius, 2);
        return distSq - minFoundDist;
    }

    @Override
    public synchronized P2d getPos(){
        return pos;
    }

    @Override
    public double getMass() {
        return mass;
    }

    @Override
    public synchronized V2d getVel() {
        return vel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setInHole(boolean b){
        inHole = b;
    }

    @Override
    public boolean isInHole(){
        return inHole;
    }

    private static void setCollider(BallImpl a, BallImpl b, String colliderType){
        // if b has a defined ball type, b is either the player or the bot => need to update a lastToCollide
        if (colliderType.equals("bot")){
            a.setLastToCollide("bot");
        } else if (colliderType.equals("player")) {
            a.setLastToCollide("player");
        } else {
            // if the colliderType is neither "bot" nor "player", we need to reset both a and b last collider
            a.setLastToCollide("");
            b.setLastToCollide("");
        }
    }

    @Override
    public synchronized void setLastToCollide(String last){
        lastToCollide = last;
    }

     @Override
     public String getLastToCollide(){
        return lastToCollide;
     }
}
