package com.barrelsofdata.springexamples.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class EventDetailsDto {
    @JsonAlias("w")
    private Integer width;
    @JsonAlias("h")
    private Integer height;
    @JsonAlias("x")
    private Float x;
    @JsonAlias("y")
    private Float y;
}
