package pcd.assignmentWithTasks.model;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountDownLatch {

    private int nProcs;
    private int nProcsDone = 0;
    private Lock lock;
    private Condition allDone;

    public CountDownLatch(int nProcs) {
        this.nProcs = nProcs;
        lock = new ReentrantLock();
        allDone = lock.newCondition();
    }

    public void await(){
        try{
            lock.lock();
            while(nProcsDone < nProcs){
                allDone.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void countDown(){
        try{
            lock.lock();
            nProcsDone++;
            if (nProcsDone == nProcs){
                allDone.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}
