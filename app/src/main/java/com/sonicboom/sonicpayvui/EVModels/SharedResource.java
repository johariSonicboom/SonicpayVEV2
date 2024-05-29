package com.sonicboom.sonicpayvui.EVModels;

public class SharedResource {
    private boolean condition = false;

    public SharedResource() {
        condition = false;
    }

    public synchronized void waitForCondition() throws InterruptedException {
        while (!condition) {
            wait(3000); // Release the lock and wait for notification
        }
        // Code to be executed when the condition is met
    }

    public synchronized void setCondition() {
        // Code to modify shared data
        condition = true;
        notify(); // Notify a waiting thread
        // or
        // notifyAll(); // Notify all waiting threads
    }
}
