package com.example.demo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitConfig {
    // Input Queue (From Simulator)
    @Bean
    public Queue inputQueue() { return new Queue("sensor_data_queue", true); }

    // Output Queues (To M4 Replicas)
    @Bean
    public List<Queue> replicaQueues() {
        List<Queue> queues = new ArrayList<>();
        queues.add(new Queue("sensor_queue_0", true));
        queues.add(new Queue("sensor_queue_1", true));
        return queues;
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}