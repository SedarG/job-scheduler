package dev.gkblt.sdr.scheduler.components;

import dev.gkblt.sdr.scheduler.model.Job;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;


class HierarchicalTimerWheelTests {

    private Job newJob(long schedule) {
        return new Job(4, 1, schedule, Optional.empty());
    }

    @Test
    void add_removeTests() {
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(1, 10, 4);
        wheel.add(newJob(3L));
        assert (wheel.entries[0][3].size() == 1);
        wheel.remove(newJob(3L));
        assert (wheel.entries[0][3].size() == 0);

        wheel.add(newJob(0L));
        assert (wheel.entries[0][0].size() == 1);
        wheel.remove(newJob(0L));
        assert (wheel.entries[0][0].size() == 0);

        wheel.add(newJob(10L));
        assert (wheel.entries[1][0].size() == 1);
        wheel.add(newJob(11L));
        assert (wheel.entries[1][0].size() == 2);
        wheel.remove(newJob(10L));
        assert (wheel.entries[1][0].size() == 1);
        wheel.remove(newJob(11L));
        assert (wheel.entries[1][0].size() == 0);

        wheel.add(newJob(90L));
        assert (wheel.entries[1][8].size() == 1);
        wheel.remove(newJob(90L));
        assert (wheel.entries[1][8].size() == 0);

        wheel.add(newJob(105L));
        assert (wheel.entries[1][9].size() == 1);
        wheel.remove(newJob(105L));
        assert (wheel.entries[1][9].size() == 0);

        wheel.add(newJob(110L));
        assert (wheel.entries[2][0].size() == 1);
        wheel.remove(newJob(110L));
        assert (wheel.entries[2][0].size() == 0);

        wheel.add(newJob(1110L));
        assert (wheel.entries[3][0].size() == 1);
        wheel.remove(newJob(1110L));
        assert (wheel.entries[3][0].size() == 0);

        wheel.add(newJob(11109L));
        assert (wheel.entries[3][9].size() == 1);
        wheel.remove(newJob(11109L));
        assert (wheel.entries[3][9].size() == 0);

        // 11110 is greater than the max that can be scheduled. add() should handle it properly
        assert (!wheel.add(newJob(11110L)));

        // Removing a non-existing Job
        assert (!wheel.remove(newJob(123L)));
    }

    @Test
    void granularityTest() {
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(10, 10, 4);
        wheel.add(newJob(3L));
        assert (wheel.entries[0][0].size() == 1);
        wheel.remove(newJob(3L));
        assert (wheel.entries[0][0].size() == 0);

        wheel.add(newJob(30L));
        assert (wheel.entries[0][3].size() == 1);
        wheel.remove(newJob(30L));
        assert (wheel.entries[0][3].size() == 0);
    }

    @Test
    void startTimestampTest() {
        TestTimeProvider time = new TestTimeProvider(0);
        time.advance(1000);
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(10, 10, 4);
        wheel.setTime(time);
        wheel.add(newJob(1003L));
        assert (wheel.entries[0][0].size() == 1);
        wheel.remove(newJob(1003L));
        assert (wheel.entries[0][0].size() == 0);

        wheel.add(newJob(1030L));
        assert (wheel.entries[0][3].size() == 1);
        wheel.remove(newJob(1030L));
        assert (wheel.entries[0][3].size() == 0);

        // 10 is less than startTimestamp. HTW.add should gracefully handle it.
        assert (!wheel.add(newJob(10L)));
        assert (!wheel.remove(newJob(10L)));
    }

    @Test
    void getMaxRecurrenceTest() {
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(1000, 75, 4);
        // 75+75*75+75*75*75+75*75*75*75 = 32068200
        assertEquals(32068200, wheel.getMaxRecurrence()) ;
    }
    @Test
    void jobsDueTest() {
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(10, 10, 4);
        for (int i = 0; i < wheel.getMaxRecurrence(); i++) {
            wheel.add(newJob(i));
        }

        int i = 0;
        for (int j = 0; j < wheel.getMaxRecurrence() / 10; j++) {
            List<Job> jobs = wheel.jobsDue();
            assertEquals (10, jobs.size());
            while(!jobs.isEmpty())  {
                Job job = jobs.remove(0);
                assertEquals(i, job.nextSchedule());
                i++;
            }
        }
    }

    @Test
    void levelBaseTest() {
        TestTimeProvider time = new TestTimeProvider(0);
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(10, 10, 4);
        int start = 300;
        time.advance(start);
        wheel.setTime(time);

        long expectedStart = start;
        for (int l=0;l<4;l++) {
            long actual = wheel.levelBase(l);
            assertEquals(expectedStart, actual, String.format("level=%d", l));
            expectedStart += Math.pow(10 , (l + 2));
        }

    }
}

