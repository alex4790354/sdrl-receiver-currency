package com.github.alex4790354.general.config.queues;

import com.github.alex4790354.general.config.RabbitBasicQueueConfig;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitCurrencyConfig extends RabbitBasicQueueConfig {

    @Value("${spring.rabbitmq.cbr.exchange}")
    private String cbrCurrencyExchange;

    @Value("${spring.rabbitmq.cbr.queue}")
    private String cbrCurrencyQueue;

    @Bean
    public Queue createCbrQueue() {
        return QueueBuilder.durable(cbrCurrencyQueue)
                .withArgument(getDeadLetterArgumentExchange(), getReceiverDeadLetterExchange())
                .withArgument(getDeadLetterRoutingKeyArgument(), getReceiverDeadLetterKey()).build();
    }

    @Bean
    Binding instanceCbrBinding(Queue createCbrQueue, FanoutExchange createCbrExchange) {
        return BindingBuilder.bind(createCbrQueue).to(createCbrExchange);
    }

    @Bean
    public FanoutExchange createCbrExchange() {
        return new FanoutExchange(cbrCurrencyExchange);
    }
}
