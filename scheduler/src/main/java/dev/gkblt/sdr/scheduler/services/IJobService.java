package dev.gkblt.sdr.scheduler.services;

import dev.gkblt.sdr.scheduler.model.Job;

import java.util.List;
import java.util.Optional;

public interface IJobService {
    Job create(Job job);

    Job update(int userId, int jobId, Optional<Long> nextSchedule, Optional<Long> recurrence);

    void delete(int userId, int jobId);

    List<Job> get(int userId, Optional<Integer> jobId);

}
