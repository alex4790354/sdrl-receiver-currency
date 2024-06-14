package com.github.alex4790354.general.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

@Getter
public class RabbitBasicQueueConfig {

    @Value("${spring.rabbitmq.dead-letter.exchange}")
    private String receiverDeadLetterExchange;

    @Value("${spring.rabbitmq.dead-letter.dead-letter-key}")
    private String receiverDeadLetterKey;

    @Value("${spring.rabbitmq.dead-letter.exchange-argument}")
    private String deadLetterArgumentExchange;

    @Value("${spring.rabbitmq.dead-letter.routing-key-argument}")
    private String deadLetterRoutingKeyArgument;
}
