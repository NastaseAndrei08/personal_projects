package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value; // Import this
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // [FIX] Inject the queue name from application.properties / environment
    @Value("${monitoring.queue.name}")
    private String myQueueName;

    // [FIX] Declare the queue this replica actually listens to
    @Bean
    public Queue sensorQueue() {
        return new Queue(myQueueName, true);
    }

    // 1. The SAME "Sync Broker"
    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange("sync_exchange");
    }

    // 2. M4's Personal Inbox (Sync)
    @Bean
    public Queue m4Inbox() {
        return new Queue("m4_sync_inbox");
    }

    // 3. Binding: M4 wants to hear about "device" events
    @Bean
    public Binding binding(Queue m4Inbox, TopicExchange syncExchange) {
        return BindingBuilder.bind(m4Inbox).to(syncExchange).with("device.#");
    }

    @Bean
    public Jackson2JsonMessageConverter converter() { return new Jackson2JsonMessageConverter(); }
}