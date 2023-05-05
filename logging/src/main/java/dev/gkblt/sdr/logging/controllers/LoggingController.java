package dev.gkblt.sdr.logging.controllers;

import dev.gkblt.sdr.logging.data.LogEntry;
import dev.gkblt.sdr.logging.services.ILogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/logs")
class LoggingController {

    @Autowired
    private ILogging service;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void log(@RequestBody LogEntry logEntry) {
        service.log(logEntry);
    }

    @GetMapping
    public List<LogEntry> get(@RequestParam Optional<Long> afterTimestamp) {
        return service.get(afterTimestamp);
    }
}
