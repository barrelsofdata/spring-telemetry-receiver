package com.barrelsofdata.springexamples.producer;

import org.springframework.kafka.KafkaException;

public interface Kafka {
    void publish(String eventRequest) throws KafkaException;
}
