package pcd.jpf;

import pcd.assignmentWithThreads.controller.CollisionMonitor;

public class CollisionMonitorTest {
    public static void main(String[] args) {
        final int N = Runtime.getRuntime().availableProcessors() + 1;
        CollisionMonitor monitor = new CollisionMonitor(N);

        for (int i = 0; i < N; i++) {
            new Thread(() -> {
                monitor.waitForOrder();
                monitor.notifyWorkDone();
            }).start();
        }

        new Thread(() -> {
            monitor.startResolvingCollisions();
            monitor.waitForCollisionsToBeResolved();
            System.out.println("Sincronizzazione completata con successo");
        }).start();
    }
}
