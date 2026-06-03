package tasks.model;

import java.util.List;

public class BoardImpl implements Board {

    private List<BallImpl> balls;
    private BallImpl playerBall;
    private BallImpl botBall;
    private Boundary bounds;
    private List<Hole> holes;
    private SpatialGridImpl grid;

    public BoardImpl(){}

    @Override
    public void init(BoardConf conf) {
        this.balls = conf.getSmallBalls();
        this.playerBall = conf.getPlayerBall();
        this.botBall = conf.getBotBall();
        this.bounds = conf.getBoardBoundary();
        this.holes = conf.getHoles();
        this.grid = new SpatialGridImpl(60, 60, bounds);
    }

    @Override
    public List<BallImpl> getBalls() {
        return balls;
    }

    @Override
    public BallImpl getPlayerBall() {
        return playerBall;
    }

    @Override
    public BallImpl getBotBall() {
        return botBall;
    }

    @Override
    public List<Hole> getHoles() {
        return holes;
    }

    @Override
    public Boundary getBounds() {
        return bounds;
    }

    @Override
    public SpatialGridImpl getGrid() {
        return grid;
    }

    public static void resolveCollision(BallImpl a, BallImpl b, String ballType) {
        if (a.isInHole()) return;
        /* check if there is a collision */
        double dx = b.getPos().x() - a.getPos().x();
        double dy = b.getPos().y() - a.getPos().y();
        double dist = Math.hypot(dx, dy);
        double minD = a.getRadius() + b.getRadius();
        /* compute dv = b.pos - a.pos vector */
        if (dist < minD && dist > 1e-6) {
            setCollider(a, b, ballType);
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
            double totalM = a.getMass() + b.getMass();
            double a_factor = overlap * (b.getMass() / totalM);
            double a_deltax = nx * a_factor;
            double a_deltay = ny * a_factor;
            a.setPos(new P2d(a.getPos().x() - a_deltax, a.getPos().y() - a_deltay));
            double b_factor = overlap * (a.getMass() / totalM);
            double b_deltax = nx * b_factor;
            double b_deltay = ny * b_factor;
            b.setPos(new P2d(b.getPos().x() + b_deltax, b.getPos().y() + b_deltay));
            /* Update velocities */
            /* relative speed along the normal vector*/
            double dvx = b.getVel().x() - a.getVel().x();
            double dvy = b.getVel().y() - a.getVel().y();
            double dvn = dvx * nx + dvy * ny;
            if (dvn <= 0) { /* if not already separating, update velocities */
                double imp = -(1 + BallImpl.RESTITUTION_FACTOR) * dvn / (1.0 / a.getMass() + 1.0 / b.getMass());
                a.kick(new V2d(a.getVel().x() - (imp / a.getMass()) * nx, a.getVel().y() - (imp / a.getMass()) * ny));
                b.kick(new V2d(b.getVel().x() + (imp / b.getMass()) * nx, b.getVel().y() + (imp / b.getMass()) * ny));
            }
        }
    }

    public static void setCollider(BallImpl a, BallImpl b, String colliderType){
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
}
