package dev.gkblt.sdr.scheduler.services;

import dev.gkblt.sdr.scheduler.components.HierarchicalTimerWheel;
import dev.gkblt.sdr.scheduler.errors.InvalidUserInput;
import dev.gkblt.sdr.scheduler.errors.ResourceNotFound;
import dev.gkblt.sdr.scheduler.model.Job;
import dev.gkblt.sdr.scheduler.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class JobService implements IJobService {
    private final Logger logger = LoggerFactory.getLogger(JobService.class);
    private final Map<Integer, Map<Integer, Job>> jobsByUser = new HashMap<>();
    private final HierarchicalTimerWheel wheel;
    private final static String JOB_EXISTS_TEMPLATE = "a job with the supplied jobId (%d) already exists for the user (%d)";
    private final static String JOB_LOG = "Running %s";
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    @Value("${logging.service.address}")
    private String loggingServiceAddress;

    private final RestTemplate restTemplate;

    @Autowired
    public JobService(HierarchicalTimerWheel wheel, RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
        this.wheel = wheel;
        executorService.schedule(this::timerRun, 1000, TimeUnit.MILLISECONDS);
    }

    void timerRun() {
        logger.trace("In timerRun");
        try {
            List<Job> dueJobs = wheel.jobsDue();
            for (Job job : dueJobs) {
                logger.debug(String.format("About to call loggerService for %s", job.toString()));
                restTemplate.postForEntity(loggingServiceAddress, new LogEntry(System.currentTimeMillis(), String.format(JOB_LOG, job.toString())), LogEntry.class);
                // TODO: reschedule if needed
            }
        } catch (Exception e) {
            logger.error("timerRun", e);
        } finally {
            executorService.schedule(this::timerRun, 1000, TimeUnit.MILLISECONDS);
        }
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
