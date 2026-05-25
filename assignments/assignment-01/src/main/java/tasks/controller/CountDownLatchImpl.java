package tasks.controller;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountDownLatchImpl implements CountDownLatch {

    private int nProcs;
    private int nProcsDone = 0;
    private Lock lock;
    private Condition allDone;

    public CountDownLatchImpl(int nProcs) {
        this.nProcs = nProcs;
        lock = new ReentrantLock();
        allDone = lock.newCondition();
    }

    @Override
    public void await() throws InterruptedException{
        try{
            lock.lock();
            while(nProcsDone < nProcs){
                allDone.await();
            }
        } catch (InterruptedException e) {
        } finally {
            lock.unlock();
        }
    }

    @Override
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
