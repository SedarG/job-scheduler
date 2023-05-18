package dev.gkblt.sdr.scheduler.controllers;

import dev.gkblt.sdr.scheduler.errors.InvalidUserInput;
import dev.gkblt.sdr.scheduler.model.Job;
import dev.gkblt.sdr.scheduler.services.IJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/users/{userId}/jobs")
class JobController {

    Logger logger = LoggerFactory.getLogger(JobController.class);
    @Autowired
    private IJobService service;

    @PostMapping
    public Job create(@RequestBody Job job) {
        logger.trace(String.format("create %s", job));
        if (job.nextSchedule() == null) {
            logger.info("nextSchedule is null");
            throw new InvalidUserInput("nextSchedule is null");
        }
        return service.create(job);
    }

    @PutMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable int userId,
                       @PathVariable int jobId,
                       @RequestParam(required = false) Optional<Long> nextSchedule,
                       @RequestParam(required = false) Optional<Long> recurrence) {
        service.update(userId, jobId, nextSchedule, recurrence);
    }

    @DeleteMapping("/{jobId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable int userId, @PathVariable int jobId) {
        service.delete(userId, jobId);
    }

    @GetMapping
    public List<Job> get(@PathVariable int userId) {
        logger.trace(String.format("get users/%s/jobs", userId ));
        return service.get(userId, Optional.empty());

    }
    @GetMapping("/{jobId}")
    public List<Job> get(@PathVariable int userId, @PathVariable int jobId) {
        logger.trace(String.format("get userId/%s/jobId/%s", userId, jobId));
        return service.get(userId, Optional.of(jobId));

    }

}