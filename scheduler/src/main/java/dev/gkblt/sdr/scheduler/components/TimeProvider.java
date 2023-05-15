package dev.gkblt.sdr.scheduler.components;

import org.springframework.stereotype.Component;

@Component
public class TimeProvider implements ITimeProvider {
    public long now() {
        return System.currentTimeMillis();
    }
}
