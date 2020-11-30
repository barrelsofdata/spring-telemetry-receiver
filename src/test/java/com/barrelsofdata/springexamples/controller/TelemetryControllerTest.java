package com.barrelsofdata.springexamples.controller;

import com.barrelsofdata.springexamples.dto.EventRequestDto;
import com.barrelsofdata.springexamples.exception.JsonConversionException;
import com.barrelsofdata.springexamples.producer.Kafka;
import com.barrelsofdata.springexamples.service.TelemetryService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class TelemetryControllerTest {
    @MockBean private TelemetryService telemetryService;
    @MockBean private Kafka kafka;
    @Autowired private MockMvc mockMvc;

    @ParameterizedTest(name = "Success API request")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}"})
    public void success(String json) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        Mockito.doNothing().when(telemetryService).receiveTelemetry(any(EventRequestDto.class));

        mockMvc.perform(
                put("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        content().contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(
                        content().string(HttpStatus.CREATED.getReasonPhrase())
                );
    }

    @ParameterizedTest(name = "Json conversion fail API response")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}"})
    public void failBadJson(String json) throws Exception {
        String expectedErrorMessage = "Failed json conversion";
        HttpHeaders headers = new HttpHeaders();
        Mockito.doThrow(new JsonConversionException(expectedErrorMessage)).when(telemetryService).receiveTelemetry(any(EventRequestDto.class));

        mockMvc.perform(
                put("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @ParameterizedTest(name = "Unsupported media type")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}}"})
    public void unsupportedMediaType(String json) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        Mockito.doNothing().when(telemetryService).receiveTelemetry(any(EventRequestDto.class));

        mockMvc.perform(
                put("/telemetry")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(json)
                        .headers(headers))
                .andExpect(
                        status().isUnsupportedMediaType()
                );
    }

    @ParameterizedTest(name = "Missing required field or wrong value for type, bad request")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"MOUSE_BUTTON_CLICK\"}"})
    public void missingRequiredField(String json) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        Mockito.doNothing().when(telemetryService).receiveTelemetry(any(EventRequestDto.class));

        mockMvc.perform(
                put("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andExpect(
                        status().isBadRequest()
                );
    }

    @ParameterizedTest(name = "Method not allowed for non-PUT requests")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}"})
    public void methodNotAllowed(String json) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        Mockito.doNothing().when(telemetryService).receiveTelemetry(any(EventRequestDto.class));

        mockMvc.perform(
                post("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andExpect(
                        status().isMethodNotAllowed()
                );
    }
}
