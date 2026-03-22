package pcd.sketch03.model;


import java.util.ArrayList;

public class Ball {

    private P2d pos;
    private V2d vel;
    private double radius;
    private double mass;
    private volatile boolean inHole = false;
    private String lastToCollide = "";

    private static double FRICTION_FACTOR = 0.25; 	/* 0 minimum */
    private static double RESTITUTION_FACTOR = 1; // mi dice quanto è elastico l'urto.


    public Ball(P2d pos, double radius, double mass, V2d vel){
        this.pos = pos;
        this.radius = radius;
        this.mass = mass;
        this.vel = vel;
    }

    public void updateState(long dt, Board ctx){
        if (this.inHole) {
            this.vel = new V2d(0, 0);
            return;
        }

        double speed = vel.abs();
        double dt_scaled = dt*0.001;
        if (speed > 0.001) {
            double dec    = FRICTION_FACTOR * dt_scaled; //decelerazione costante
            double factor = Math.max(0, speed - dec) / speed;
            vel = vel.mul(factor);
        } else {
            vel = new V2d(0,0);
        }
        pos = pos.sum(vel.mul(dt_scaled));

        if (checkInHole(ctx.getBounds())){
            this.setInHole(true);
            this.vel = new V2d(0,0);
            return;
        }
        applyBoundaryConstraints(ctx); //ho coordinate logiche, non pixel: siamo nel model
    }

    public void kick(V2d vel) {
        this.vel = vel;
    }

    /**
     *
     * Keep the ball inside the boundaries, updating the velocity in the case of bounces
     *
     * @param ctx
     */
    private void applyBoundaryConstraints(Board ctx){
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

    /**
     *
     * Resolving collision between 2 balls, updating their position and velocity
     *
     * @param a
     * @param b
     * @param ballIndex: the index of the small ball
     * @param ballType: the type of the big ball colliding with the small one: if it gets in a hole, we need to know which player
     *                sent it in.
     */
    public static void resolveCollision(Ball a, Ball b, int ballIndex, String ballType) {
        // define the order of the locks basing on the hashcode to prevent deadlock
        if (a.getInHole()) return;
        Ball first = (a.hashCode() < b.hashCode()) ? a : b;
        Ball second = (first == b) ? a : b;

        synchronized (first) {
            synchronized (second) {

                /* check if there is a collision */
                double dx = second.pos.x() - first.pos.x();
                double dy = second.pos.y() - first.pos.y();
                double dist = Math.hypot(dx, dy);
                double minD = first.radius + second.radius;

                /* compute dv = b.pos - a.pos vector */
                if (dist < minD && dist > 1e-6) {
                    if (ballType.equals("bot")){
                        a.setLastToCollide("bot");
                    } else if (ballType.equals("player")) {
                        a.setLastToCollide("player");
                    }
                    /*
                     * Collision case - what to do:
                     * 1) solve overlaps, moving balls
                     * 2) update velocities
                     */
                    double nx = dx / dist;
                    double ny = dy / dist;

                    /*
                     *
                     * Update positions to solve overlaps, moving balls along dvn
                     * - the displacements is proportional to the mass
                     *
                     */
                    double overlap = minD - dist;
                    double totalM = first.mass + second.mass;

                    double first_factor = overlap * (second.mass / totalM);
                    double first_deltax = nx * first_factor;
                    double first_deltay = ny * first_factor;

                    first.pos = new P2d(first.getPos().x() - first_deltax, first.getPos().y() - first_deltay);

                    double second_factor = overlap * (first.mass / totalM);
                    double second_deltax = nx * second_factor;
                    double second_deltay = ny * second_factor;

                    second.pos = new P2d(second.getPos().x() + second_deltax, second.getPos().y() + second_deltay);

                    /* Update velocities */

                    /* relative speed along the normal vector*/
                    double dvx = second.vel.x() - first.vel.x();
                    double dvy = second.vel.y() - first.vel.y();
                    double dvn = dvx * nx + dvy * ny;

                    if (dvn <= 0) { /* if not already separating, update velocities */
                        double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0 / first.getMass() + 1.0 / second.getMass());

                        first.vel = new V2d(first.vel.x() - (imp / first.mass) * nx, first.vel.y() - (imp / first.mass) * ny);
                        second.vel = new V2d(second.vel.x() + (imp / second.mass) * nx, second.vel.y() + (imp / second.mass) * ny);
                    }
                }
            }
        }
    }


    public boolean checkInHole(Boundary b){
        // 1. RECUPERA LE COORDINATE REALI
        double px = pos.x();
        double py = pos.y();
        double r = this.radius;

        // Definiamo una tolleranza fissa basata sulla scala del tuo mondo (-1.5 a 1.5)
        // 0.20 è una misura che "aggancia" bene la palla senza essere troppo punitiva
        double tolerance = 0.60;

        // Controllo se è vicina al bordo superiore
        boolean nearTop = Math.abs(py - b.y0()) < tolerance;

        // Controllo se è vicina a uno dei due angoli laterali
        boolean nearLeft = Math.abs(px - b.x0()) < tolerance;
        boolean nearRight = Math.abs(px - b.x1()) < tolerance;

        if (nearTop && (nearLeft || nearRight)) {
            // Se entra qui, la palla DEVE sparire
            this.setInHole(true);
            return true;
        }

        return false;
    }

    public P2d getPos(){
        return pos;
    }

    public double getMass() {
        return mass;
    }

    public V2d getVel() {
        return vel;
    }

    public double getRadius() {
        return radius;
    }

    public void setInHole(boolean b){
        inHole = b;
    }

    public boolean getInHole(){
        return inHole;
    }

    public void setLastToCollide(String last){
        lastToCollide = last;
    }

     public String getLastToCollide(){
        return lastToCollide;
     }
}
