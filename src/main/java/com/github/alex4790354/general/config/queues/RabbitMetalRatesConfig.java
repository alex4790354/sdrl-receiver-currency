package com.github.alex4790354.general.config.queues;

import com.github.alex4790354.general.config.RabbitBasicQueueConfig;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMetalRatesConfig extends RabbitBasicQueueConfig {

    @Value("${spring.rabbitmq.metal.exchange}")
    private String cbrMetalRatesExchange;

    @Value("${spring.rabbitmq.metal.queue}")
    private String cbrMetalRatesQueue;

    @Bean
    public Queue createRatesQueue() {
        return QueueBuilder.durable(cbrMetalRatesQueue)
                .withArgument(getDeadLetterArgumentExchange(), getReceiverDeadLetterExchange())
                .withArgument(getDeadLetterRoutingKeyArgument(), getReceiverDeadLetterKey()).build();
    }

    @Bean
    Binding instanceMetalRatesBinding(Queue createRatesQueue, FanoutExchange createMetalRatesExchange) {
        return BindingBuilder.bind(createRatesQueue).to(createMetalRatesExchange);
    }

    @Bean
    public FanoutExchange createMetalRatesExchange() {
        return new FanoutExchange(cbrMetalRatesExchange);
    }
}
