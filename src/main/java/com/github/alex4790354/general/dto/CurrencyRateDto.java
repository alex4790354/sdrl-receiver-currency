package com.github.alex4790354.general.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class CurrencyRateDto {

    @JsonProperty("id")
    protected String id;

    @JsonProperty("num_code")
    protected int numCode;

    @JsonProperty("char_code")
    protected String charCode;

    @JsonProperty("nominal")
    protected long nominal;

    @JsonProperty("name")
    protected String name;

    @JsonProperty("value")
    protected BigDecimal value;

    @JsonProperty("date")
    protected String dateAsString; //  in "yyyy-MM-dd" format.

    public LocalDate getEffectiveDate() {
        return LocalDate.parse(dateAsString,
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
