package tasks.model;

import java.util.List;

public interface Ball {

    void updateState(long dt, BoardImpl ctx);

    void kick(V2d vel);

    void applyImpulse(V2d delta);

    boolean checkInHole(List<Hole> holes);

    double distFromHole(Hole h);

    P2d getPos();

    void setPos(P2d pos);

    double getMass();

    V2d getVel();

    double getRadius();

    void setInHole(boolean b);

    boolean isInHole();

    void setLastToCollide(String last);

    String getLastToCollide();
}
