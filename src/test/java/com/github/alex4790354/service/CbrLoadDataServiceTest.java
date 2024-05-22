package com.github.alex4790354.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alex4790354.general.dto.CurrencyDto;
import com.github.alex4790354.general.dto.CurrencyRateDto;
import com.github.alex4790354.general.dto.MetalRateDto;
import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class CbrLoadDataServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private IDatabaseTester databaseTester;

    @BeforeEach
    public void setUp() throws Exception {
        // DBUnit setup
        databaseTester = new JdbcDatabaseTester(
                "org.hsqldb.jdbc.JDBCDriver",
                "jdbc:hsqldb:file:src/test/resources/testdb/sdrldb;schema=CBR",
                "usr",
                "pwd"
        );
        jdbcTemplate.execute("DELETE FROM cbr.currency_rate_history WHERE nominal > -1");
        jdbcTemplate.execute("DELETE FROM cbr.currency_rate WHERE nominal > -1");
        jdbcTemplate.execute("DELETE FROM cbr.currency WHERE frequency = 'DAILY'");
        jdbcTemplate.execute("DELETE FROM cbr.metal_rate WHERE buy > 0");
    }

    @Test
    public void testLoadCurrency() throws Exception {

        int count = jdbcTemplate.queryForObject("SELECT count(*) FROM cbr.currency", Integer.class);
        assertThat(count).isEqualTo(0);

        String jsonContent = new String(Files.readAllBytes(Paths.get("src/test/resources/sdrl-feed-example/currency-list.json")));
        ObjectMapper objectMapper = new ObjectMapper();
        List<CurrencyDto> currencyList = objectMapper.readValue(jsonContent, new TypeReference<List<CurrencyDto>>() {});

        assertThat(currencyList).isNotEmpty();
        loadCurrency(currencyList);

        count = jdbcTemplate.queryForObject("SELECT count(*) FROM cbr.currency", Integer.class);
        assertThat(count).isEqualTo(currencyList.size());

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/sdrl-expected-datasets/currency-list.xml"));
        ITable expectedTable = expectedDataSet.getTable("currency");
        QueryDataSet actualDataSet = new QueryDataSet(databaseTester.getConnection());
        actualDataSet.addTable("CURRENCY", "SELECT * FROM CBR.CURRENCY ORDER BY id");
        ITable actualTable = actualDataSet.getTable("CURRENCY");
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(expectedTable, new String[]{"creation_time", "update_time", "instr_name", "instr_name_eng"});
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(actualTable, new String[]{"creation_time", "update_time", "instr_name", "instr_name_eng"});

        Assertion.assertEquals(filteredExpectedTable, filteredActualTable);
    }


    @Test
    public void testLoadCurrencyRate() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String currencyRateJson = new String(Files.readAllBytes(Paths.get("src/test/resources/sdrl-feed-example/currency-rate.json")));
        List<CurrencyRateDto> currencyRateList = objectMapper.readValue(currencyRateJson, new TypeReference<List<CurrencyRateDto>>() {});
        assertThat(currencyRateList).isNotEmpty();

        loadCurrencyRates(currencyRateList);
        int count = jdbcTemplate.queryForObject("SELECT count(*) FROM cbr.currency_rate", Integer.class);
        assertThat(count).isEqualTo(currencyRateList.size());

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/sdrl-expected-datasets/currency-rate.xml"));
        ITable expectedTable = expectedDataSet.getTable("currency_rate");

        QueryDataSet actualDataSet = new QueryDataSet(databaseTester.getConnection());
        actualDataSet.addTable("currency_rate", "SELECT * FROM cbr.currency_rate ORDER BY id");
        ITable actualTable = actualDataSet.getTable("currency_rate");
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(expectedTable, new String[]{"creation_time", "update_time"});
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(actualTable, new String[]{"creation_time", "update_time"});

        Assertion.assertEquals(filteredExpectedTable, filteredActualTable);
    }


    @Test
    public void testLoadMetalRate() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String metalRateJson = new String(Files.readAllBytes(Paths.get("src/test/resources/sdrl-feed-example/metal-rate.json")));
        List<MetalRateDto> metalRateList = objectMapper.readValue(metalRateJson, new TypeReference<>(){});
        assertThat(metalRateList).isNotEmpty();

        loadMetalRates(metalRateList);
        int count = jdbcTemplate.queryForObject("SELECT count(*) FROM cbr.metal_rate", Integer.class);
        assertThat(count).isEqualTo(metalRateList.size());

        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/sdrl-expected-datasets/metal-rate.xml"));
        ITable expectedTable = expectedDataSet.getTable("metal_rate");

        QueryDataSet actualDataSet = new QueryDataSet(databaseTester.getConnection());
        actualDataSet.addTable("metal_rate", "SELECT * FROM cbr.metal_rate ORDER BY code");
        ITable actualTable = actualDataSet.getTable("metal_rate");
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(expectedTable, new String[]{"creation_time", "update_time"});
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(actualTable, new String[]{"creation_time", "update_time"});

        Assertion.assertEquals(filteredExpectedTable, filteredActualTable);
    }


    // HSQLDB does not support option "ON CONFLICT (...)" and PostgreSQL not support Merge => we have to write separate script for HSQLDB
    private void loadCurrency(List<CurrencyDto> currencyList) {
        try {
            for (CurrencyDto currency : currencyList) {
                jdbcTemplate.update("""
                                            MERGE INTO cbr.currency AS target
                                                USING (VALUES (?, ?, ?, ?, ?, ?)) AS source (id, frequency, name_rus, name_eng, nominal, parent_code)
                                                ON target.id = source.id
                                                WHEN MATCHED THEN UPDATE SET
                                                    target.frequency = source.frequency,
                                                    target.name_rus = source.name_rus,
                                                    target.name_eng = source.name_eng,
                                                    target.nominal = source.nominal,
                                                    target.parent_code = source.parent_code,
                                                    target.update_time = NOW()
                                                WHEN NOT MATCHED THEN INSERT (id, frequency, name_rus, name_eng, nominal, parent_code)
                                                    VALUES (source.id, source.frequency, source.name_rus, source.name_eng, source.nominal, source.parent_code);
                                        """,
                                        currency.getId(), currency.getFrequency(), currency.getNameRus(), currency.getNameEng(), currency.getNominal(), currency.getParentCode()
                                    );
            }
        } catch (Exception e) {
            System.out.println("Error (loadCurrency). Message --> " + e.getMessage());
        }
    }


    private void loadCurrencyRates(List<CurrencyRateDto> currencyRateList) {
        try {
            for (CurrencyRateDto rate : currencyRateList) {
                jdbcTemplate.update("""
                MERGE INTO cbr.currency_rate AS target
                USING (VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, 'RUB', ?)) AS source (id, effective_date, nominal, first_crncy, second_crncy, value)
                ON target.id = source.id AND target.effective_date = source.effective_date
                WHEN MATCHED THEN UPDATE SET
                    target.nominal = source.nominal,
                    target.first_crncy = source.first_crncy,
                    target.second_crncy = source.second_crncy,
                    target.value = source.value,
                    target.update_time = NOW()
                WHEN NOT MATCHED THEN INSERT (id, effective_date, nominal, first_crncy, second_crncy, value, creation_time, update_time)
                    VALUES (source.id, source.effective_date, source.nominal, source.first_crncy, source.second_crncy, source.value, NOW(), NOW());
            """,
                        rate.getId(), rate.getDateAsString(), rate.getNominal(), rate.getCharCode(), rate.getValue());
            }
        } catch (Exception e) {
            System.out.println("Error (loadCurrencyRates). Message --> " + e.getMessage());
        }
    }


    private void loadMetalRates(List<MetalRateDto> metalRateList) {
        try {
            for (MetalRateDto rate : metalRateList) {
                jdbcTemplate.update("""
                MERGE INTO cbr.metal_rate AS target
                USING (VALUES (?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)) AS source (code, effective_date, buy, sell, value)
                ON target.code = source.code AND target.effective_date = source.effective_date
                WHEN MATCHED THEN UPDATE SET
                    target.buy = source.buy,
                    target.sell = source.sell,
                    target.value = source.value,
                    target.update_time = NOW()
                WHEN NOT MATCHED THEN INSERT (code, effective_date, buy, sell, value, creation_time, update_time)
                    VALUES (source.code, source.effective_date, source.buy, source.sell, source.value, NOW(), NOW());
            """,
                        rate.getCode(), rate.getDateAsString(), rate.getBuy(), rate.getSell(), rate.getValue());
            }
        } catch (Exception e) {
            System.out.println("Error (loadCurrencyRates). Message --> " + e.getMessage());
        }
    }
}