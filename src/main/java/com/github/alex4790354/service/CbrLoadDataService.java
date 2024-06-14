package com.github.alex4790354.service;


import com.github.alex4790354.general.config.exception.CbrException;
import com.github.alex4790354.general.dto.CurrencyDto;
import com.github.alex4790354.general.dto.CurrencyRateDto;
import com.github.alex4790354.general.dto.MetalRateDto;
import com.github.alex4790354.repository.ReceiverCbrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CbrLoadDataService {

    private final ReceiverCbrRepository receiverCbrRepository;

    public void loadCurrency(List<CurrencyDto> currenciesList) {
        try {
            receiverCbrRepository.saveCurrenciesList(currenciesList);
        } catch (Exception e) {
            log.error("\nError (loadCurrency).\n Message --> {}", e.getMessage());
            throw new CbrException("Exception in (loadCurrency)" + e.getMessage());
        }
    }


    public void loadCurrencyRates(List<CurrencyRateDto> rateList) {
        try {
            receiverCbrRepository.saveRatesListWithHistory(rateList);
        } catch (Exception e) {
            log.error("\nError (loadCurrency).\n Message --> {}", e.getMessage());
            throw new CbrException("Exception in (loadCurrency)" + e.getMessage());
        }
    }


    public void loadMetalRates(List<MetalRateDto> metalRateDtoList) {
        try {
            receiverCbrRepository.saveMetalRates(metalRateDtoList);
        } catch (Exception e) {
            log.error("\nError (loadMetalRates).\n Message --> {}", e.getMessage());
            throw new CbrException("Exception in (loadMetalRates)" + e.getMessage());
        }
    }
}
