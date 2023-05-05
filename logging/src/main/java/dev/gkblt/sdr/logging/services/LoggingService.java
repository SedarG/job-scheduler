package dev.gkblt.sdr.logging.services;

import dev.gkblt.sdr.logging.data.LogEntry;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Service
public class LoggingService implements ILogging {

    PriorityQueue<LogEntry> entries = new PriorityQueue<>(new Comparator<LogEntry>() {
        @Override
        public int compare(LogEntry a, LogEntry b) {
            return (int)(a.timestamp() - b.timestamp());
        }
    });

    @Override
    public void log(LogEntry entry) {
       entries.offer(entry);
    }

    @Override
    public List<LogEntry> get(Optional<Long> afterTimestamp) {
        return entries.stream()
                .takeWhile((e) -> e.timestamp() >= afterTimestamp.orElse(0L))
                .collect(Collectors.toList());
    }
}
