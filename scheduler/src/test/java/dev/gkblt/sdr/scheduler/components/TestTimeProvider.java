package dev.gkblt.sdr.scheduler.components;

public class TestTimeProvider implements ITimeProvider {

    private long time;
    public TestTimeProvider(long time) {
        this.time = time;
    }

    public void advance(long duration) {
        this.time += duration;
    }

    public long now() {
        return this.time;
    }
}
