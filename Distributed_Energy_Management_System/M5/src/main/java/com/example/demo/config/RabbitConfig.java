package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 1. Define the queue for receiving notifications
    @Bean
    public Queue notificationQueue() {
        return new Queue("notification_queue", true); // true = durable
    }

    // 2. Define the Topic Exchange (must match the one in M4)
    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange("sync_exchange");
    }

    // 3. Bind the queue to the exchange
    // Listens for keys like "notification.alert" or "notification.overconsumption"
    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange syncExchange) {
        return BindingBuilder.bind(notificationQueue).to(syncExchange).with("notification.#");
    }

    // 4. ESSENTIAL: JSON Converter
    // Allows us to receive Map<String, Object> instead of byte[]
    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}