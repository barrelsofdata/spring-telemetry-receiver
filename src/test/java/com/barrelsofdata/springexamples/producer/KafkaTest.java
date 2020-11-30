package com.barrelsofdata.springexamples.producer;

import com.barrelsofdata.springexamples.dto.EventRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class KafkaTest {
    @MockBean private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired @InjectMocks private KafkaImpl producer;

    @Autowired private ObjectMapper mapper;


    @ParameterizedTest(name = "Check successful send")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}"})
    public void successSend(String json) throws JsonProcessingException {
        EventRequestDto eventRequestDto = mapper.readValue(json, EventRequestDto.class);
        String kafkaPayload = mapper.writeValueAsString(eventRequestDto);
        Mockito.doReturn(new SettableListenableFuture<>()).when(kafkaTemplate).send(any(String.class), any(String.class));
        Assertions.assertDoesNotThrow(() -> producer.publish(kafkaPayload));
    }

    @ParameterizedTest(name = "Check failed send")
    @ValueSource(strings = {"{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}}","{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"}"})
    public void failedSend(String json) throws JsonProcessingException, InterruptedException {
        EventRequestDto eventRequestDto = mapper.readValue(json, EventRequestDto.class);
        String kafkaPayload = mapper.writeValueAsString(eventRequestDto);
        Mockito.doThrow(KafkaException.class).when(kafkaTemplate).send(any(String.class), any(String.class));
        Assertions.assertThrows(KafkaException.class, () -> producer.publish(kafkaPayload));
    }

}
