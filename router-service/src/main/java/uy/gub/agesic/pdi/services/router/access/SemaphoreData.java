package uy.gub.agesic.pdi.services.router.access;

import java.util.concurrent.Semaphore;

public class SemaphoreData {

    private Semaphore semaphore;

    private int permits;

    public SemaphoreData(int permits) {
        semaphore = new Semaphore(permits);
        this.permits = permits;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public int getPermits() {
        return permits;
    }

}
