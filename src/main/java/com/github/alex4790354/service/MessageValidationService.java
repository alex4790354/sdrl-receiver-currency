package com.github.alex4790354.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alex4790354.general.config.exception.ReceiverValidationException;
import com.github.alex4790354.general.dto.CurrencyDto;
import com.github.alex4790354.general.dto.CurrencyRateDto;
import com.github.alex4790354.general.dto.MetalRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class MessageValidationService {

    private final ObjectMapper objectMapper;


    public List<CurrencyDto> validateAndParseCbrData(List<Map<String, Object>> mapMessageList) {
        try {
            return objectMapper.convertValue(mapMessageList, new TypeReference<List<CurrencyDto>>() {});
        } catch (Exception e) {
            log.error("Error converting List Map message {} to list IndexStaticDto. (validateAndParseCbrData)\nException: {}", mapMessageList, e.getMessage());
            throw new ReceiverValidationException("Error converting List Map message" + mapMessageList + " to List<CurrencyDto>.\nException: " + e.getMessage());
        }
    }


    public List<CurrencyRateDto> validateAndParseCurrencyRateData(List<Map<String, Object>> mapMessageList) {
        try {
            return objectMapper.convertValue(mapMessageList, new TypeReference<List<CurrencyRateDto>>() {});
        } catch (Exception e) {
            log.error("Error converting List Map message {} to list IndexStaticDto. (validateAndParseCurrencyRateData)\nException: {}", mapMessageList, e.getMessage());
            throw new ReceiverValidationException("Error converting List Map message" + mapMessageList + " to List<CurrencyRateDto>.\nException: " + e.getMessage());
        }
    }


    public List<MetalRateDto> validateAndParseMetalRateData(List<Map<String, Object>> mapMessageList) {
        try {
            return objectMapper.convertValue(mapMessageList, new TypeReference<List<MetalRateDto>>() {});
        } catch (Exception e) {
            log.error("Error converting List Map message {} to list MetalRateDtoList. (validateAndParseMetalRateData)\nException: {}", mapMessageList, e.getMessage());
            throw new ReceiverValidationException("Error converting List Map message" + mapMessageList + " to MetalRateDtoList.\nException: " + e.getMessage());
        }
    }
}
