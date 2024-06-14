package com.github.alex4790354.service.rabbitmqlistener;

import com.github.alex4790354.general.config.exception.ReceiverValidationException;
import com.github.alex4790354.service.CbrLoadDataService;
import com.rabbitmq.client.Channel;
import com.github.alex4790354.general.dto.CurrencyRateDto;
import com.github.alex4790354.service.MessageValidationService;
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
public class CurrencyRateListener extends AbstractErrorMessageProducer {

    private final MessageValidationService validationService;
    private final CbrLoadDataService cbrLoadDataService;

    @Value("${spring.rabbitmq.rates.name}")
    private String cbrRatesName;

    @RabbitListener(queues = "${spring.rabbitmq.rates.queue}")
    public void listenRatesExchange(List<Map<String, Object>> mapMessageList,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        log.debug("Listen CurrencyRatesList --> {}", mapMessageList);
        try {
            List<CurrencyRateDto> rateList = validationService.validateAndParseCurrencyRateData(mapMessageList);
            log.info("We just got: CurrencyRatesList.size(): " + rateList.size());
            cbrLoadDataService.loadCurrencyRates(rateList);
            channel.basicAck(tag, false);
        } catch (ReceiverValidationException validationException) {
            sendingErrorMessage(validationException, cbrRatesName, mapMessageList);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("""
                    CurrencyRatesList cannot be handled: {}.
                    With exception --> {}
                    Returning to the queue...
                    """, mapMessageList, e.getMessage());
            channel.basicNack(tag, false, true);
            Thread.sleep(timeout);
        }
    }

}
