package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 1. Define the Exchange (The "Megaphone")
    @Bean
    public TopicExchange syncExchange() {
        return new TopicExchange("sync_exchange");
    }

    // 2. Define M3's Private Inbox (Queue)
    @Bean
    public Queue m3Inbox() {
        return new Queue("m3_sync_inbox");
    }

    // 3. Define the Binding (The "Subscription")
    // "Connect m3Inbox to syncExchange and filter for 'user.#'"
    @Bean
    public Binding binding(Queue m3Inbox, TopicExchange syncExchange) {
        return BindingBuilder.bind(m3Inbox).to(syncExchange).with("user.#");
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}