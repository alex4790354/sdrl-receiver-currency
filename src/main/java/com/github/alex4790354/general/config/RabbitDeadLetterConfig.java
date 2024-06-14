package com.github.alex4790354.general.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitDeadLetterConfig {

    @Value("${spring.rabbitmq.dead-letter.queue}")
    private String receiverDeadLetterQueue;

    @Value("${spring.rabbitmq.dead-letter.exchange}")
    private String receiverDeadLetterExchange;

    @Value("${spring.rabbitmq.dead-letter.dead-letter-key}")
    private String receiverDeadLetterKey;

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(receiverDeadLetterQueue).build();
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(receiverDeadLetterExchange, true, false);
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(receiverDeadLetterKey);
    }
}
