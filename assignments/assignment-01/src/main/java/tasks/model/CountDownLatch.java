package tasks.model;

public interface CountDownLatch {

    void await() throws InterruptedException;

    void countDown();
}
