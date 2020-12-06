package com.barrelsofdata.springexamples.producer;

public interface Kafka {
    void publish(String eventRequest);
}
