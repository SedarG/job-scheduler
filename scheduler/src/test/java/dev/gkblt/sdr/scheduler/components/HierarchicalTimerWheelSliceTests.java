package dev.gkblt.sdr.scheduler.components;

import dev.gkblt.sdr.scheduler.model.Job;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HierarchicalTimerWheelSliceTests {
    private Job newJob(long schedule) {
        return new Job(4, 1, schedule, Optional.empty());
    }

    @Test
    void add_removeTests() {
        HierarchicalTimerWheelSlice wheel = new HierarchicalTimerWheelSlice(0, 1, 10, 4);
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
        HierarchicalTimerWheelSlice wheel = new HierarchicalTimerWheelSlice(0,10, 10, 4);
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
        HierarchicalTimerWheelSlice wheel = new HierarchicalTimerWheelSlice(1000, 10, 10, 4);

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
        HierarchicalTimerWheelSlice wheel = new HierarchicalTimerWheelSlice(0,1000, 75, 4);
        // 75+75*75+75*75*75+75*75*75*75 = 32068200
        // since granularity is 1000, the maxRecurrence must match
        // 32068200000
        assertEquals(32068200000L, wheel.getMaxRecurrence()) ;
    }
    @Test
    void jobsDueTest() {
        HierarchicalTimerWheelSlice wheel = new HierarchicalTimerWheelSlice(0,10, 10, 4);


        // insert one job with each timestamp. Since each bucket's granularity is 10 units, it'll be inserting 10 jobs to each bucket.
        // getMaxRecurrence
        long jobsCount = wheel.getMaxRecurrence();

        for (int i = 0; i < jobsCount; i++) {
            wheel.add(newJob(i));
        }

        int i = 0;
        for (int j = 0; j < jobsCount / 10; j++) {
            List<Job> jobs = wheel.jobsDue();
            assertEquals (10, jobs.size());
            while(!jobs.isEmpty())  {
                Job job = jobs.remove(0);
                assertEquals(i, job.nextSchedule());
                i++;
            }
        }
        assertEquals(jobsCount, i);
    }


    @Test
    void levelBaseTest() {
        HierarchicalTimerWheelSlice wheel = new HierarchicalTimerWheelSlice(300, 10, 10, 4);

        long expectedStart = 300;
        for (int l=0;l<4;l++) {
            long actual = wheel.levelBase(l);
            assertEquals(expectedStart, actual, String.format("level=%d", l));
            expectedStart += Math.pow(10 , (l + 2));
        }

    }
}
