package dev.gkblt.sdr.scheduler.services;

import dev.gkblt.sdr.scheduler.components.HierarchicalTimerWheel;
import dev.gkblt.sdr.scheduler.errors.InvalidUserInput;
import dev.gkblt.sdr.scheduler.errors.ResourceNotFound;
import dev.gkblt.sdr.scheduler.model.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class JobService implements IJobService {
    private final Map<Integer, Map<Integer, Job>> jobsByUser = new HashMap<>();
    private final HierarchicalTimerWheel wheel;
    private final static String JOB_EXISTS_TEMPLATE = "a job with the supplied jobId (%d) already exists for the user (%d)";
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public JobService(HierarchicalTimerWheel wheel) {
        this.wheel = wheel;
        executorService.schedule(this::timerRun, 1000, TimeUnit.MILLISECONDS);
    }

    void timerRun() {
        List<Job> dueJobs = wheel.jobsDue();
        // TODO: call logging service
    }

    @Override
    public Job create(Job job) {

        if (jobsByUser.containsKey(job.userId()) && jobsByUser.get(job.userId()).containsKey(job.jobId())) {
            throw new InvalidUserInput(String.format(JOB_EXISTS_TEMPLATE, job.jobId(), job.userId()));
        }
        if (!jobsByUser.containsKey(job.userId())) {
            jobsByUser.put(job.userId(), new HashMap<Integer, Job>());
        }
        jobsByUser.get(job.userId()).put(job.jobId(), job);
        wheel.add(job);
        return job;
    }

    @Override
    public Job update(int userId, int jobId, Optional<Long> nextSchedule, Optional<Long> recurrence) {
        if (!jobsByUser.containsKey(userId) || !jobsByUser.get(userId).containsKey(jobId)) {
            if (nextSchedule.orElse(null) == null) {
                throw new InvalidUserInput("Can't update a non-existing job with an empty nextSchedule");
            }
            return create(new Job(userId, jobId, nextSchedule.get(), recurrence));
        }
        // TODO: validate the parameters
        Job existingJob = jobsByUser.get(userId).get(jobId);
        Job newJob = new Job(userId, jobId, nextSchedule.orElse(existingJob.nextSchedule()), recurrence.or(existingJob::recurrence));
        jobsByUser.get(userId).put(jobId, newJob);
        wheel.remove(existingJob);
        wheel.add(newJob);
        return newJob;
    }

    @Override
    public void delete(int userId, int jobId) {
        if (!jobsByUser.containsKey(userId) || !jobsByUser.get(userId).containsKey(jobId)) {
            throw new ResourceNotFound();
        }
        Job existingJob = jobsByUser.get(userId).get(jobId);

        jobsByUser.get(userId).remove(jobId);
        wheel.remove(existingJob);
    }

    @Override
    public List<Job> get(int userId, Optional<Integer> jobId) {
        if (!jobsByUser.containsKey(userId) ||
                (jobId.isPresent() &&  !jobsByUser.get(userId).containsKey(jobId.get()))) {
            throw new ResourceNotFound();
        }
        return jobId.map(id -> List.of(jobsByUser.get(userId).get(id))).orElseGet(() -> jobsByUser.get(userId).values().stream().toList());
    }

}
