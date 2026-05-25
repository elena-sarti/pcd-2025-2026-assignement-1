package tasks.model;

import java.util.List;

public interface Ball {
    /**
     *
     * Resolving collision between 2 balls, updating their position and velocity
     *
     * @param a
     * @param b
     * @param ballType: the type of the big ball colliding with the small one: if it gets in a hole, we need to know which player
     *                  sent it in.
     */
    static void resolveCollision(BallImpl a, BallImpl b, String ballType) {
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

    void updateState(long dt, BoardImpl ctx);

    void kick(V2d vel);

    boolean checkInHole(List<Hole> holes);

    double distFromHole(Hole h);

    P2d getPos();

    double getMass();

    V2d getVel();

    double getRadius();

    void setInHole(boolean b);

    boolean isInHole();

    void setLastToCollide(String last);

    String getLastToCollide();
}
