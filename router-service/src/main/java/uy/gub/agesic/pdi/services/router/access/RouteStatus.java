package uy.gub.agesic.pdi.services.router.access;

import java.util.Arrays;

class RouteStatus {

    private boolean degradable;

    private int countData;

    private int index;

    private long[] data;

    private long total;

    private boolean degraded;

    private boolean hasTotalMetrics;

    public RouteStatus(int count, boolean degradable) {
        this.countData = count;
        data = new long[count];
        this.degradable = degradable;
    }

    public synchronized boolean checkDegradation(long data, long timeout) {
        total = total - this.data[index];
        total = total + data;
        this.data[index] = data;
        if (!hasTotalMetrics && (index + 1) >= countData) {
            hasTotalMetrics = true;
        }
        index = (index + 1) % countData;

        if (hasTotalMetrics) {
            degraded = (total / countData) >= timeout;
        }
        return degraded;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public boolean isDegradable() {
        return degradable;
    }

    public void setDegradable(boolean degradable) {
        this.degradable = degradable;
    }

    @Override
    public String toString() {
        return "RouteStatus{" +
                "degradable=" + degradable +
                ", degraded=" + degraded +
                ", countData=" + countData +
                ", index=" + index +
                ", data=" + Arrays.toString(data) +
                ", total=" + total +
                '}';
    }
}
