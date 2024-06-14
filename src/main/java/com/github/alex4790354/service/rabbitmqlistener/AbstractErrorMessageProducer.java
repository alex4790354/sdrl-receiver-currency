package com.github.alex4790354.service.rabbitmqlistener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alex4790354.general.config.exception.ReceiverValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractErrorMessageProducer {
    protected RabbitTemplate rabbitTemplate;
    protected ObjectMapper objectMapper;

    @Value("${spring.rabbitmq.invalid-message.exchange}")
    protected String errorExchange;

    @Value("${spring.rabbitmq.invalid-message.invalid-message-key}")
    protected String errorKey;

    @Value("${spring.rabbitmq.nack.timeout}")
    protected int timeout;

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    protected void sendingErrorMessage(ReceiverValidationException validationException,
                                       String queueName,
                                       List<Map<String, Object>> mapMessageList) {
        log.error("Receiver Ndx Sending invalid message to '{}' --> {} \n " + "With exception: {}",
                errorExchange,
                mapMessageList,
                validationException.getMessage());
    }
}
