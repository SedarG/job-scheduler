package dev.gkblt.sdr.scheduler.components;

import dev.gkblt.sdr.scheduler.model.Job;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class HierarchicalTimerWheelTests {

    private Job newJob(long schedule) {
        return new Job(4, 1, schedule, Optional.empty());
    }

    @Test
    void jobsDueAlternatesPrimaryAndSecondary() {
        TestTimeProvider time = new TestTimeProvider(0);
        HierarchicalTimerWheel wheel = new HierarchicalTimerWheel(time);

        Job theJob = newJob(100);
        wheel.add(theJob);
        for (int year = 0; year < 4; year++) {
            time.advance(1000);
            List<Job> jobs = wheel.jobsDue();
            assertEquals(1, jobs.size());
            assertEquals(theJob, jobs.get(0));
            theJob = newJob(theJob.nextSchedule() + HierarchicalTimerWheel.MAX_RECURRENCE);
            wheel.add(theJob);

            while (time.now() % HierarchicalTimerWheel.MAX_RECURRENCE != 0) {
                time.advance(1000);
                jobs = wheel.jobsDue();
                assertNotNull(jobs);
                assertEquals(0, jobs.size());
            }

        }

    }

}

