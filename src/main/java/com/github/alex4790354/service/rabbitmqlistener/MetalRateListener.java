package com.github.alex4790354.service.rabbitmqlistener;

import com.github.alex4790354.general.config.exception.ReceiverValidationException;
import com.github.alex4790354.service.CbrLoadDataService;
import com.github.alex4790354.service.MessageValidationService;
import com.rabbitmq.client.Channel;
import com.github.alex4790354.general.dto.MetalRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.rates.enable", havingValue = "true")
@RequiredArgsConstructor
public class MetalRateListener extends AbstractErrorMessageProducer {

    private final MessageValidationService validationService;
    private final CbrLoadDataService cbrLoadDataService;


    @Value("${spring.rabbitmq.metal.name}")
    private String cbrMetalRatesName;

    @RabbitListener(queues = "${spring.rabbitmq.metal.queue}")
    public void listenRatesExchange(List<Map<String, Object>> mapMessageList,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        log.debug("Listen MetalRatesList --> {}", mapMessageList);
        try {
            List<MetalRateDto> metalRateDtoList = validationService.validateAndParseMetalRateData(mapMessageList);
            log.info("We just got: MetalRatesList.size(): " + metalRateDtoList.size());
            cbrLoadDataService.loadMetalRates(metalRateDtoList);
            channel.basicAck(tag, false);
        } catch (ReceiverValidationException validationException) {
            sendingErrorMessage(validationException, cbrMetalRatesName, mapMessageList);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("""
                    MetalRateDtoList cannot be handled: {}.
                    With exception --> {}
                    Returning to the queue...
                    """, mapMessageList, e.getMessage());
            channel.basicNack(tag, false, true);
            Thread.sleep(timeout);
        }
    }

}
