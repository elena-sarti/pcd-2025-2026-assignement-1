package pcd.sequentialVersion.controller;

public interface BoundedBuffer<Item> {

    void put(Item item) throws InterruptedException;
    
    Item get() throws InterruptedException;
    
}
