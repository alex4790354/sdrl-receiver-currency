package com.github.alex4790354.general.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Objects;

@Data
public class CurrencyDto {

    @JsonProperty("id")
    protected String id;

    @JsonProperty("name_rus")
    protected String nameRus;

    @JsonProperty("name_eng")
    protected String nameEng;

    @JsonProperty("nominal")
    protected long nominal;

    @JsonProperty("parent_code")
    protected String parentCode;

    @JsonProperty("frequency")
    protected String frequency;

    @JsonProperty("char_code")
    protected String charCode;

    public CurrencyDto(String id, String nameRus, String nameEng, long nominal, String parentCode, String frequency, String charCode) {
        this.id = id;
        this.nameRus = nameRus;
        this.nameEng = nameEng;
        this.nominal = nominal;
        this.parentCode = parentCode;
        this.frequency = frequency;
        this.charCode = Objects.requireNonNullElse(charCode, "xx???xx");
    }
}
