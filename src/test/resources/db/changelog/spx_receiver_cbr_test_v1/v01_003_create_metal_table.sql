--liquibase formatted sql
--changeset alex4790354:01_003

CREATE TABLE IF NOT EXISTS cbr.metal
(
    source_code       int,
    source            VARCHAR(20) NOT NULL,
    instr_name        VARCHAR(100) NOT NULL,
    instr_name_eng    VARCHAR(100) NOT NULL,
    created           TIMESTAMP DEFAULT now(),
    CONSTRAINT PK_METAL PRIMARY KEY (source_code)
);

INSERT INTO cbr.metal(source_code, source, instr_name, instr_name_eng, created)
VALUES  (1, 'METAU-CBRF', 'Золото, учетные цены на драгоценные металлы', 'Gold Reference Prices', now()),
        (2, 'METAG-CBRF', 'Серебро, учетные цены на драгоценные металлы', 'Silver Reference Prices', now()),
        (3, 'METPT-CBRF', 'Платина, учетные цены на драгоценные металлы', 'Platinum Reference Prices', now()),
        (4, 'METPD-CBRF', 'Палладий, учетные цены на драгоценные металлы', 'Palladium Reference Prices', now());


CREATE TABLE IF NOT EXISTS cbr.metal_rate
(
    code                int,
    effective_date      DATE,
    buy                 decimal(14,4) NOT NULL,
    sell                decimal(14,4) NOT NULL,
    value               decimal(14,4),
    creation_time       TIMESTAMP WITH TIME ZONE DEFAULT now(),
    update_time         TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT PK_METAL_RATE PRIMARY KEY (code, effective_date),
    CONSTRAINT FK_METAL_RATE FOREIGN KEY (code) REFERENCES cbr.metal (source_code)
);

