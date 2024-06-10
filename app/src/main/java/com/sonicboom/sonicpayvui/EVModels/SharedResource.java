package com.sonicboom.sonicpayvui.EVModels;

public class SharedResource {
    private boolean condition = false;

    public SharedResource() {
        condition = false;
    }

    public synchronized void waitForCondition(int duration) throws InterruptedException {
        while (!condition) {
            if (duration > 0) { wait(duration); break; } else { wait(); }
        }
    }

    public synchronized void setCondition() {
        // Code to modify shared data
        condition = true;
        notify(); // Notify a waiting thread
        // or
        // notifyAll(); // Notify all waiting threads
    }
}
