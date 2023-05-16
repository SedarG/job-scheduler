package dev.gkblt.sdr.scheduler.controllers;

import dev.gkblt.sdr.scheduler.model.Job;
import dev.gkblt.sdr.scheduler.services.IJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/users/{userId}/jobs")
class JobController {

    @Autowired
    private IJobService service;

    @PostMapping
    public Job create(Job job) {
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

    @GetMapping("/{jobId}")
    public List<Job> get(@PathVariable int userId, @PathVariable(required = false) Optional<Integer> jobId) {
        return service.get(userId, jobId);
    }

}
