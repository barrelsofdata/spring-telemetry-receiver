package com.barrelsofdata.springexamples;

import com.barrelsofdata.springexamples.service.TelemetryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EmbeddedKafka
@AutoConfigureMockMvc
public class ApplicationIntegrationTest {
    private int NUMBER_OF_BROKERS = 2;
    private boolean CONTROLLER_SHUTDOWN = false;
    private int NUMBER_OF_PARTITIONS = 2;
    @Value("${spring.kafka.producer.topic}")
    private String TOPIC;

    @Autowired private TelemetryService telemetryService;

    @Autowired private MockMvc mockMvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker = new EmbeddedKafkaBroker(NUMBER_OF_BROKERS, CONTROLLER_SHUTDOWN, NUMBER_OF_PARTITIONS, TOPIC);

    private BlockingQueue<ConsumerRecord<String, String>> records;
    private KafkaMessageListenerContainer<String, String> container;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerConfigs = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfigs, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterEach
    void tearDown() {
        container.stop();
    }

    @ParameterizedTest(name = "Integration: API request success")
    @CsvSource(value = {
            "{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}};{\"timestamp\":\"2020-11-25T09:53:14.000+00:00\",\"id\":123,\"type\":\"LEFT_MOUSE_BUTTON_CLICK\",\"payload\":{\"width\":213,\"height\":124,\"x\":1000.0,\"y\":5000.0}}",
            "{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"};{\"timestamp\":\"2020-11-25T09:53:14.000+00:00\",\"id\":123,\"type\":\"RIGHT_MOUSE_BUTTON_CLICK\",\"payload\":null}"}
            , delimiter = ';')
    public void success(String inputJson, String kafkaJson) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        mockMvc.perform(
                put("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inputJson)
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

        Thread.sleep(1000);
        ConsumerRecord<String, String> singleRecord = records.poll(100, TimeUnit.MILLISECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Assertions.assertThat(singleRecord.key()).isNull();
        Assertions.assertThat(singleRecord.value()).isEqualTo(kafkaJson);
    }

    @ParameterizedTest(name = "Integration: API request success")
    @CsvSource(value = {
            "{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"LEFT_MOUSE_BUTTON_CLICK\",\"pl\":{\"x\":1000,\"y\":5000,\"w\":213,\"h\":124}};{\"timestamp\":\"2020-11-25T09:53:14.000+00:00\",\"id\":123,\"type\":\"LEFT_MOUSE_BUTTON_CLICK\",\"payload\":{\"width\":213,\"height\":124,\"x\":1000.0,\"y\":5000.0}}",
            "{\"ts\":\"1606297994000\",\"id\":\"123\",\"ty\":\"RIGHT_MOUSE_BUTTON_CLICK\"};{\"timestamp\":\"2020-11-25T09:53:14.000+00:00\",\"id\":123,\"type\":\"RIGHT_MOUSE_BUTTON_CLICK\",\"payload\":null}"}
            , delimiter = ';')
    public void unsupportedMedia(String inputJson, String kafkaJson) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        mockMvc.perform(
                put("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inputJson)
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

        Thread.sleep(1000);
        ConsumerRecord<String, String> singleRecord = records.poll(100, TimeUnit.MILLISECONDS);
        Assertions.assertThat(singleRecord).isNotNull();
        Assertions.assertThat(singleRecord.key()).isNull();
        Assertions.assertThat(singleRecord.value()).isEqualTo(kafkaJson);
    }

}
