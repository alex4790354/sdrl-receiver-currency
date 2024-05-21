--liquibase formatted sql
--changeset alex4790354:01_001

CREATE SCHEMA CBR;
SET SCHEMA CBR;

CREATE TABLE IF NOT EXISTS CBR.CURRENCY
(
    id                  VARCHAR(10),
    frequency           VARCHAR(10) NOT NULL,
    name_rus            VARCHAR(250) NOT NULL,
    name_eng            VARCHAR(250) NOT NULL,
    nominal             BIGINT NOT NULL,
    parent_code         VARCHAR(10),
    creation_time       TIMESTAMP WITH TIME ZONE DEFAULT now(),
    update_time         TIMESTAMP WITH TIME ZONE DEFAULT now(),
    instr_name          VARCHAR(250),
    instr_name_eng      VARCHAR(250),
    CONSTRAINT PK_CURRENCY PRIMARY KEY (ID)
);



CREATE TABLE IF NOT EXISTS CURRENCY_RATE
(
    id                  VARCHAR(10),
    effective_date      DATE,
    first_crncy         VARCHAR(3) NOT NULL,
    second_crncy        VARCHAR(3) NOT NULL,
    nominal             BIGINT NOT NULL,
    value               DECIMAL(14,4),
    creation_time       TIMESTAMP WITH TIME ZONE DEFAULT now(),
    update_time         TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT PK_CURRENCY_RATE PRIMARY KEY (ID),
    CONSTRAINT FK_currency_rate FOREIGN KEY (id) REFERENCES CURRENCY (id)
);

DROP TABLE CURRENCY_RATE_HISTORY;
CREATE TABLE IF NOT EXISTS CURRENCY_RATE_HISTORY
(
     id                  VARCHAR(10),
     effective_date      DATE,
     first_crncy         VARCHAR(3) NOT NULL,
     second_crncy        VARCHAR(3) NOT NULL,
     nominal             BIGINT NOT NULL,
     value               DECIMAL(14,4),
     creation_time       TIMESTAMP with time zone DEFAULT now(),
     archived_at         TIMESTAMP with time zone,
     CONSTRAINT PK_CURRENCY_RATE_HISTORY PRIMARY KEY (id, effective_date),
     CONSTRAINT FK_CURRENCY_RATE_HISTORY FOREIGN KEY (id) REFERENCES CBR.CURRENCY (id)
);

--DROP TABLE CURRENCY_RATE_HISTORY;
--DROP TABLE CURRENCY_RATE;
--DROP TABLE CURRENCY;
