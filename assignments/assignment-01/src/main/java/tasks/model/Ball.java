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
    void resolveCollision(BallImpl a, BallImpl b, String ballType);

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
