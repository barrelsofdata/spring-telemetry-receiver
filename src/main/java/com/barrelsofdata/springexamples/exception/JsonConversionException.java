package com.barrelsofdata.springexamples.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class JsonConversionException extends RuntimeException {
    public JsonConversionException() {
        super();
    }
    public JsonConversionException(String message) {
        super(message);
    }
    public JsonConversionException(String message, Exception e) {
        super(message, e);
    }
}
