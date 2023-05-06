package dev.gkblt.sdr.scheduler.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidUserInput extends RuntimeException{

    public InvalidUserInput(String message) {
        super(message);
    }
}
