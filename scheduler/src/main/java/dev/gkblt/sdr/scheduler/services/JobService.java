package dev.gkblt.sdr.scheduler.services;

import dev.gkblt.sdr.scheduler.errors.InvalidUserInput;
import dev.gkblt.sdr.scheduler.errors.ResourceNotFound;
import dev.gkblt.sdr.scheduler.model.Job;

import java.util.*;
public class JobService implements IJobService {

    /**
     * granularity is the size of the bucket of the lowest Hierarchical Timer Wheel (HTW)
     */
    private final static int granularity = 1000;
    private final Map<Integer, Map<Integer, Job>> jobsByUser = new HashMap<>();
    private final static String JOB_EXISTS_TEMPLATE = "a job with the supplied jobId (%d) already exists for the user (%d)";

    @Override
    public Job create(Job job) {

        if (jobsByUser.containsKey(job.userId()) && jobsByUser.get(job.userId()).containsKey(job.jobId())) {
            throw new InvalidUserInput(String.format(JOB_EXISTS_TEMPLATE, job.jobId(), job.userId()));
        }
        if (!jobsByUser.containsKey(job.userId())) {
            jobsByUser.put(job.userId(), new HashMap<Integer, Job>());
        }
        jobsByUser.get(job.userId()).put(job.jobId(), job);
        return job;
    }

    @Override
    public Job update(int userId, int jobId, Long nextSchedule, Long recurrence) {
        if (!jobsByUser.containsKey(userId) || !jobsByUser.get(userId).containsKey(jobId)) {
            if (nextSchedule == null) {
                throw new InvalidUserInput("Can't update a non-existing job with an empty nextSchedule");
            }
            return create(new Job(userId, jobId, nextSchedule, Optional.of(recurrence)));
        }
        // TODO: validate the parameters
        Job job = new Job(userId, jobId, nextSchedule, Optional.of(recurrence));
        jobsByUser.get(userId).put(jobId, job);
        return job;
    }

    @Override
    public void delete(int userId, int jobId) {
        if (!jobsByUser.containsKey(userId) || !jobsByUser.get(userId).containsKey(jobId)) {
            throw new ResourceNotFound();
        }
        jobsByUser.get(userId).remove(jobId);
    }

    @Override
    public Job get(int userId, int jobId) {
        if (!jobsByUser.containsKey(userId) || !jobsByUser.get(userId).containsKey(jobId)) {
            throw new ResourceNotFound();
        }
        return jobsByUser.get(userId).get(jobId);
    }
}
