package dev.gkblt.sdr.scheduler.model;

import java.util.Optional;

public record Job(
        Integer userId,
        Integer jobId,
        Long nextSchedule,
        Optional<Long> recurrence) { }

