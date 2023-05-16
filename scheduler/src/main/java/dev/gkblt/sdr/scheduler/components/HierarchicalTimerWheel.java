package dev.gkblt.sdr.scheduler.components;

import dev.gkblt.sdr.scheduler.model.Job;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HierarchicalTimerWheel {

    private final ITimeProvider time;
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final ArrayList<Consumer<List<Job>>> callbackFunctions;
    final static long MAX_RECURRENCE = 32068200000L;

    private HierarchicalTimerWheelSlice primary;
    private HierarchicalTimerWheelSlice secondary;

    @Autowired
    public HierarchicalTimerWheel(ITimeProvider time) {
        this.callbackFunctions = new ArrayList<>();
        this.time = time;
        long startTime = time.now();
        this.primary = new HierarchicalTimerWheelSlice(startTime);
        this.secondary = new HierarchicalTimerWheelSlice(this.primary.getStartTimestamp() + MAX_RECURRENCE);
    }

    public boolean remove(Job job) {
        return primary.remove(job);
    }

    public boolean add(Job job) {
        if (job.nextSchedule() < primary.getStartTimestamp() ) {
            return false;
        }
        if (job.recurrence().orElse(0L) >= MAX_RECURRENCE) {
            return false;
        }
        if (job.nextSchedule() < secondary.getStartTimestamp()) {
            return primary.add(job);
        }
        return secondary.add(job);
    }

    public void registerCallback(Consumer<List<Job>> callbackFunction) {
        this.callbackFunctions.add(callbackFunction);
    }

    public void deregisterCallback(Consumer<List<Job>> callbackFunction) {
        this.callbackFunctions.remove(callbackFunction);
    }

    void timerRun() {
        List<Job> dueJobs = jobsDue();
        for (Consumer<List<Job>> callback : callbackFunctions) {
            callback.accept(dueJobs);
        }
    }

    List<Job> jobsDue() {
        List<Job> jobs = primary.jobsDue();
        if (jobs == null) {
            assert(time.now() >= secondary.getStartTimestamp());
            this.primary = this.secondary;
            this.secondary = new HierarchicalTimerWheelSlice(this.primary.getStartTimestamp() + MAX_RECURRENCE);
            jobs = primary.jobsDue();
        }
        return jobs;
    }

}
