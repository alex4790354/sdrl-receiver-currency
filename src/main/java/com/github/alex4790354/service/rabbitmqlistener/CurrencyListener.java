package com.github.alex4790354.service.rabbitmqlistener;

import com.github.alex4790354.general.config.exception.ReceiverValidationException;
import com.github.alex4790354.service.CbrLoadDataService;
import com.rabbitmq.client.Channel;
import com.github.alex4790354.general.dto.CurrencyDto;
import com.github.alex4790354.service.MessageValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;



@Slf4j
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.cbr.enable", havingValue = "true")
@RequiredArgsConstructor
public class CurrencyListener extends AbstractErrorMessageProducer {

    private final MessageValidationService validationService;
    private final CbrLoadDataService cbrLoadDataService;

    @Value("${spring.rabbitmq.cbr.name}")
    private String cbrProductName;

    @RabbitListener(queues = "${spring.rabbitmq.cbr.queue}")
    public void listenPlaceExchange(List<Map<String, Object>> mapMessageList,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        log.debug("Listen CurrencyList with--> {}", mapMessageList);
        try {
            List<CurrencyDto> currenciesList = validationService.validateAndParseCbrData(mapMessageList);
            log.info("We just got: currenciesList.size(): " + currenciesList.size());
            cbrLoadDataService.loadCurrency(currenciesList);
            channel.basicAck(tag, false);
        } catch (ReceiverValidationException validationException) {
            sendingErrorMessage(validationException, cbrProductName, mapMessageList);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("""
                    CurrenciesList cannot be handled: {}.
                    With exception --> {}
                    Returning to the queue...
                    """, mapMessageList, e.getMessage());
            channel.basicNack(tag, false, true);
            Thread.sleep(timeout);
        }
    }

}
