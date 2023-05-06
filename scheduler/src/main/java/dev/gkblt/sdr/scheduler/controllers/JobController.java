package dev.gkblt.sdr.scheduler.controllers;

import dev.gkblt.sdr.scheduler.services.IJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/jobs")
class JobController {

    @Autowired
    private IJobService service;
}
