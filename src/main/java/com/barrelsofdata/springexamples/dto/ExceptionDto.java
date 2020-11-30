package com.barrelsofdata.springexamples.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Date;

@Builder
public class ExceptionDto {
    @JsonProperty("timestamp")
    private Date timestamp;
    @JsonProperty("error")
    private String error;
}
