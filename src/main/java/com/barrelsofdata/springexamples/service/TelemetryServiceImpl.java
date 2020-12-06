package com.barrelsofdata.springexamples.service;

import com.barrelsofdata.springexamples.dto.EventRequestDto;
import com.barrelsofdata.springexamples.exception.JsonConversionException;
import com.barrelsofdata.springexamples.producer.Kafka;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

@Service
public class TelemetryServiceImpl implements TelemetryService {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryServiceImpl.class);

    @Autowired private Kafka producer;

    @Autowired private ObjectMapper jsonMapper;

    @Override
    public void receiveTelemetry(EventRequestDto eventRequest) {
        try {
            String payload = jsonMapper.writeValueAsString(eventRequest);
            producer.publish(payload);
        } catch (JsonProcessingException e) {
            logger.error("Unable to convert message to json {}", eventRequest);
            throw new JsonConversionException("Failed json conversion");
        } catch (KafkaException e) {
            logger.error("Kafka exception for request {}", eventRequest);
            // Handle what you want to do with the data here
        }
    }
}
