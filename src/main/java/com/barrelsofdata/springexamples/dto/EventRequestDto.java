package com.barrelsofdata.springexamples.dto;

import com.barrelsofdata.springexamples.constants.EventType;
import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@ToString
@Setter
@Getter
public class EventRequestDto {
    @NotNull
    @JsonAlias("ts")
    private Timestamp timestamp;
    @NotNull
    @JsonAlias("id")
    private Integer id;
    @NotNull
    @JsonAlias("ty")
    private EventType type;
    @JsonAlias("pl")
    private EventDetailsDto payload;
}
