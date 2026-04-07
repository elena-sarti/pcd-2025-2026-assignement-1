package pcd.assignmentWithThreads.model;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CollisionMonitor {
    private final int nThreads;
    private int nFinished = 0;
    private boolean startSignal = false;
    Lock lock;
    Condition readyToStart;
    Condition allFinished;

    public CollisionMonitor(int nThreads) {
        this.nThreads = nThreads;
        lock = new ReentrantLock();
        readyToStart = lock.newCondition();
        allFinished = lock.newCondition();
    }

    public void startResolvingCollisions() {
        try {
            lock.lock();
            nFinished = 0;
            startSignal = true;
            readyToStart.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void waitForCollisionsToBeResolved() {
        try {
            lock.lock();
            while (nFinished < nThreads) {
                allFinished.await();
            }
            startSignal = false;
        } catch (InterruptedException ex) {
        } finally {
            lock.unlock();
        }
    }

    public void waitForOrder() {
        try {
            lock.lock();
            while (!startSignal) {
                readyToStart.await();
            }
        } catch (InterruptedException ex) {
        } finally {
            lock.unlock();
        }
    }

    public void notifyWorkDone() {
        try {
            lock.lock();
            nFinished++;
            if (nFinished == nThreads) {
                allFinished.signal();
            }
        } catch (RuntimeException ex) {
        } finally {
            lock.unlock();
        }
    }
}

