package dev.gkblt.sdr.scheduler.services;

import dev.gkblt.sdr.scheduler.model.Job;
import org.junit.jupiter.api.Test;

class HierarchicalTimerWheelTests {

    private Job newJob(long schedule) {
        return new Job(4,1,schedule,null);
    }

    @Test
    void add() {
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(1, 10, 4, 0);
        wheel.add(newJob(3L));
        assert(wheel.entries[0][3].size() == 1);

        wheel.add(newJob(0L));
        assert(wheel.entries[0][0].size() == 1);

        wheel.add(newJob(10L));
        assert(wheel.entries[1][0].size() == 1);
        wheel.add(newJob(11L));
        assert(wheel.entries[1][0].size() == 2);

        wheel.add(newJob(90L));
        assert(wheel.entries[1][8].size() == 1);

        wheel.add(newJob(105L));
        assert(wheel.entries[1][9].size() == 1);

        wheel.add(newJob(110L));
        assert(wheel.entries[2][0].size() == 1);

        wheel.add(newJob(1110L));
        assert(wheel.entries[3][0].size() == 1);

        wheel.add(newJob(11109L));
        assert(wheel.entries[3][9].size() == 1);
    }
}