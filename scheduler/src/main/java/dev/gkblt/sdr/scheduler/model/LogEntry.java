package dev.gkblt.sdr.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LogEntry(long timestamp, String entry) {
}
