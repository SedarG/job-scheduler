package dev.gkblt.sdr.logging.services;

import dev.gkblt.sdr.logging.data.LogEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoggingServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void log() {
    }

    @Test
    void get() {
        long time = 0;
        LoggingService service = new LoggingService();
        assertEquals(List.of(),service.get(Optional.empty()));
        service.log(new LogEntry(++time, String.format("%d",time)));
        assertEquals(List.of(new LogEntry(time, String.format("%d",time))),service.get(Optional.empty()));
        while(time < 1000) {
            service.log(new LogEntry(++time, String.format("%d", time)));
        }
        assertEquals(1000, service.get(Optional.empty()).size());
        assertEquals(300, service.get(Optional.of(700L)).size());
        assertEquals(time, service.get(Optional.of(time-1)).get(0).timestamp());
    }

}