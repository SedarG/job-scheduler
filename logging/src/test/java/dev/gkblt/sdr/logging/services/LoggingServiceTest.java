package dev.gkblt.sdr.logging.services;

import dev.gkblt.sdr.logging.data.LogEntry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:unittest.properties"
)
class LoggingServiceTest {

    @Autowired
    private ILogging service;

    @Test
    void log() {
        long time = 0;
        while(time < 1000) {
            service.log(new LogEntry(++time, String.format("%d", time)));
        }
        assertEquals(100, service.get(Optional.empty()).size());
    }

    @Test
    void get() {
        long time = 0;
        assertEquals(List.of(),service.get(Optional.empty()));
        service.log(new LogEntry(++time, String.format("%d",time)));
        assertEquals(List.of(new LogEntry(time, String.format("%d",time))),service.get(Optional.empty()));
        while(time < 1000) {
            service.log(new LogEntry(++time, String.format("%d", time)));
        }
        var all = service.get(Optional.empty());
        assertEquals(100, all.size());
        var last30 = service.get(Optional.of(970L));
        assertEquals(30, last30.size());
        assertEquals(time, service.get(Optional.of(time-1)).get(0).timestamp());
    }

}