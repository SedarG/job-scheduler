package dev.gkblt.sdr.logging.services;

import dev.gkblt.sdr.logging.data.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class LoggingService implements ILogging {

    Logger logger = LoggerFactory.getLogger(LoggingService.class);
    @Value("${log.buffer.capacity}")
    private int logBufferCapacity;
    TreeMap<Long, LogEntry> entries = new TreeMap<>();

    @Override
    public void log(LogEntry entry) {
       entries.put(entry.timestamp(),entry);
       while (entries.size() > logBufferCapacity) {
           entries.pollFirstEntry();
       }
    }

    @Override
    public List<LogEntry> get(Optional<Long> afterTimestamp) {
        return List.copyOf(entries.tailMap(afterTimestamp.orElse(0L)+1).values());
    }
}
