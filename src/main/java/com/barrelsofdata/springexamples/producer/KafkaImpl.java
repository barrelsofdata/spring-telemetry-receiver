package com.barrelsofdata.springexamples.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class KafkaImpl implements Kafka {
    private static final Logger logger = LoggerFactory.getLogger(Kafka.class);

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.producer.topic}")
    private String topic;

    public void publish(String payload) throws KafkaException {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, payload); // Blocks call if kafka broker isn't available/responding
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.info("Message published to Kafka partition {} with offset {}", result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to publish message {}", payload, ex);
            }
        });
    }
}
