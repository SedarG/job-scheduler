package dev.gkblt.sdr.logging.services;

import dev.gkblt.sdr.logging.data.LogEntry;

import java.util.List;
import java.util.Optional;

public interface ILogging {
    public void log(LogEntry entry);

    public List<LogEntry> get(Optional<Long> afterTimestamp);
}
