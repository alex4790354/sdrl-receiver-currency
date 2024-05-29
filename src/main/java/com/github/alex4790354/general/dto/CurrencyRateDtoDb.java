package com.github.alex4790354.general.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class CurrencyRateDtoDb {

    protected String id;

    protected LocalDate effectiveDate;

    protected long nominal;

    protected String firstCrncy;

    protected String secondCrncy;

    protected BigDecimal value;

    protected OffsetDateTime creationTime;

}
