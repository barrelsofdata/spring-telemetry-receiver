package com.barrelsofdata.springexamples.controller;

import com.barrelsofdata.springexamples.dto.EventRequestDto;
import com.barrelsofdata.springexamples.exception.JsonConversionException;
import com.barrelsofdata.springexamples.producer.Kafka;
import com.barrelsofdata.springexamples.service.TelemetryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.RequestDispatcher;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class ApplicationErrorControllerTest {
    @MockBean private TelemetryService telemetryService;
    @MockBean private Kafka kafka;
    @Autowired private MockMvc mockMvc;

    @ParameterizedTest(name = "Error API request")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}"})
    void testError(String json) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        Mockito.doThrow(JsonConversionException.class).when(telemetryService).receiveTelemetry(any(EventRequestDto.class));

        mockMvc.perform(
                put("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andDo(result -> {
                    if (result.getResolvedException() != null) {
                        byte[] response = mockMvc.perform(
                                get("/error")
                                        .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, result.getResponse().getStatus())
                                        .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, Objects.requireNonNull(result.getRequest().getRequestURI()))
                                        .requestAttr(RequestDispatcher.ERROR_EXCEPTION, result.getResolvedException())
                                        .requestAttr(RequestDispatcher.ERROR_MESSAGE, String.valueOf(result.getResponse().getErrorMessage())))
                                .andReturn()
                                .getResponse()
                                .getContentAsByteArray();
                        result.getResponse()
                                .getOutputStream()
                                .write(response);
                    }
                })
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.error").value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.timestamp").exists()
                );
    }

    @Test
    @DisplayName("Test default error status code")
    void testErrorWithoutMessage() throws Exception {
        mockMvc.perform(
                get("/error"))
                .andExpect(
                        status().isInternalServerError()
                );
    }

    @ParameterizedTest(name = "Error API request")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\""})
    void testNotFound(String json) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        mockMvc.perform(
                put("/nonexisting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .headers(headers))
                .andExpect(
                        status().isNotFound()
                );
    }
}
