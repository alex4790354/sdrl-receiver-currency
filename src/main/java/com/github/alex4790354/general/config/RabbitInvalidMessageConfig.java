package com.github.alex4790354.general.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitInvalidMessageConfig {

    @Value("${spring.rabbitmq.invalid-message.queue}")
    private String invalidMessageQueue;

    @Value("${spring.rabbitmq.invalid-message.exchange}")
    private String invalidMessageExchange;

    @Value("${spring.rabbitmq.invalid-message.invalid-message-key}")
    private String invalidMessageKey;

    @Bean
    public Queue invalidMessageQueue() {
        return QueueBuilder.durable(invalidMessageQueue).build();
    }

    @Bean
    DirectExchange invalidMessageExchange() {
        return new DirectExchange(invalidMessageExchange, true, false);
    }

    @Bean
    Binding invalidMessageBinding() {
        return BindingBuilder.bind(invalidMessageQueue()).to(invalidMessageExchange()).with(invalidMessageKey);
    }
}
