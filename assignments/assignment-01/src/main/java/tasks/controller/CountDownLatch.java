package tasks.controller;

public interface CountDownLatch {
    void await() throws InterruptedException;

    void countDown();
}
