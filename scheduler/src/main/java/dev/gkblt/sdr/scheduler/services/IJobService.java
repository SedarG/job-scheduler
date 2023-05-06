package dev.gkblt.sdr.scheduler.services;

import dev.gkblt.sdr.scheduler.model.Job;

public interface IJobService {
    Job create(Job job);

    Job update(int userId, int jobId, Long nextSchedule, Long recurrence);

    void delete(int userId, int jobId);

    Job get(int userId, int jobId);
}
