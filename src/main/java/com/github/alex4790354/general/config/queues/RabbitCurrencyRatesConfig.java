package com.github.alex4790354.general.config.queues;

import com.github.alex4790354.general.config.RabbitBasicQueueConfig;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitCurrencyRatesConfig extends RabbitBasicQueueConfig {

    @Value("${spring.rabbitmq.rates.exchange}")
    private String cbrCurrencyRatesExchange;

    @Value("${spring.rabbitmq.rates.queue}")
    private String cbrCurrencyRatesQueue;

    @Bean
    public Queue createCurrencyRatesQueue() {
        return QueueBuilder.durable(cbrCurrencyRatesQueue)
                .withArgument(getDeadLetterArgumentExchange(), getReceiverDeadLetterExchange())
                .withArgument(getDeadLetterRoutingKeyArgument(), getReceiverDeadLetterKey()).build();
    }

    @Bean
    Binding instanceCurrencyRatesBinding(Queue createCurrencyRatesQueue, FanoutExchange createCurrencyRatesExchange) {
        return BindingBuilder.bind(createCurrencyRatesQueue).to(createCurrencyRatesExchange);
    }

    @Bean
    public FanoutExchange createCurrencyRatesExchange() {
        return new FanoutExchange(cbrCurrencyRatesExchange);
    }
}
