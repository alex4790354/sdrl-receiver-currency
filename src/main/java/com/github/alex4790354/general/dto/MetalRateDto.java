package com.github.alex4790354.general.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
@NoArgsConstructor
public class MetalRateDto {

    @Setter
    @JsonProperty("code")
    private int code;

    @Setter
    @JsonProperty("date")
    private String dateAsString; // in ISO-date format: yyyy-mm-dd. Example: "2023-mm-dd"

    @JsonProperty("buy")
    private BigDecimal buy;

    @JsonProperty("sell")
    private BigDecimal sell;

    private BigDecimal value;

    public void setBuy(BigDecimal buy) {
        this.buy = buy;
        if (null != this.sell && null != buy && !this.sell.equals(BigDecimal.ZERO) && !buy.equals(BigDecimal.ZERO)) {
            if (this.sell.equals(buy)) {
                this.value = buy;
            } else {
                this.value = BigDecimal.ZERO;
            }
        }
    }

    public void setSell(BigDecimal sell) {
        this.sell = sell;
        if (null != this.buy && null != sell && !this.buy.equals(BigDecimal.ZERO) && !sell.equals(BigDecimal.ZERO)) {
            if (this.buy.equals(sell)) {
                this.value = sell;
            } else {
                this.value = BigDecimal.ZERO;
            }
        }
    }

}
