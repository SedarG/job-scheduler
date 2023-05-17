package dev.gkblt.sdr.scheduler.model;

import java.util.Optional;

public record Job(
        Integer userId,
        Integer jobId,
        Long nextSchedule,
        Optional<Long> recurrence) {

    @Override
    public String toString() {
        return String.format("{userID: %s, jobId: %s, nextSchedule: %s, recurrence: %s",
                userId == null ? "null" : userId.toString(),
                jobId == null ? "null" : jobId.toString(),
                nextSchedule == null ? "null" : nextSchedule.toString(),
                recurrence.map(Object::toString).orElse("None"));
    }
}

