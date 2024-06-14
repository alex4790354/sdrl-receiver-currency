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

    @Autowired
    private CbrLoadDataService cbrLoadDataService;

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

        cbrLoadDataService.loadCurrency(currencyList);
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

        cbrLoadDataService.loadCurrencyRates(currencyRateList);
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

        cbrLoadDataService.loadMetalRates(metalRateList);
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
}