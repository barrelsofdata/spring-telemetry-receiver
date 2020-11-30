package com.barrelsofdata.springexamples.service;

import com.barrelsofdata.springexamples.dto.EventRequestDto;
import com.barrelsofdata.springexamples.exception.JsonConversionException;
import com.barrelsofdata.springexamples.producer.Kafka;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TelemetryServiceTest {
    @MockBean private Kafka kafka;
    @MockBean private ObjectMapper mapper;
    @Autowired @InjectMocks private TelemetryServiceImpl telemetryService;

    @ParameterizedTest(name = "Successful publish")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}"})
    public void successPublish(String json) throws JsonProcessingException {
        EventRequestDto eventRequestDto = new ObjectMapper().readValue(json, EventRequestDto.class);
        telemetryService.receiveTelemetry(eventRequestDto);
    }

    @ParameterizedTest(name = "Json conversion failure")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}"})
    public void failPublish(String json) throws JsonProcessingException {
        EventRequestDto eventRequestDto = new ObjectMapper().readValue(json, EventRequestDto.class);
        Mockito.doThrow(JsonProcessingException.class).when(mapper).writeValueAsString(any(EventRequestDto.class));
        JsonConversionException exception = Assertions.assertThrows(JsonConversionException.class, () -> telemetryService.receiveTelemetry(eventRequestDto));
        Assertions.assertEquals("Failed json conversion", exception.getMessage());
    }

}
