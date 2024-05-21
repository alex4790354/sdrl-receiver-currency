package com.github.alex4790354.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alex4790354.general.dto.CurrencyDto;
import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
        // Настройка DBUnit
        databaseTester = new JdbcDatabaseTester(
                "org.hsqldb.jdbc.JDBCDriver",
                //"jdbc:hsqldb:mem:sdrldb",
                "jdbc:hsqldb:file:src/test/resources/testdb/sdrldb;schema=CBR",
                "usr",
                "pwd"
        );

        /*IDataSet dataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/sdrl-expected-datasets/currency-list.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.onSetup();*/
    }

    @Test
    //@Disabled
    public void testDatabaseOperation() throws Exception {

        jdbcTemplate.update("DELETE FROM cbr.currency");

        ObjectMapper objectMapper = new ObjectMapper();
        MessageValidationService service = new MessageValidationService(objectMapper);

        // Читаем содержимое файла в строку
        String jsonContent = new String(Files.readAllBytes(Paths.get("src/test/resources/sdrl-feed-example/currency-list.json")));

        // Используем существующий метод сервиса для преобразования JSON строки в список CurrencyDto
        List<CurrencyDto> currencyList = objectMapper.readValue(jsonContent, new TypeReference<List<CurrencyDto>>() {});
        assertThat(currencyList).isNotEmpty();

        // Загрузка нового списка валют с использованием CbrLoadDataService
        //cbrLoadDataService.loadCurrency(currencyList);
        loadCurrency(currencyList);
        //CbrLoadDataService service1 = new CbrLoadDataService();

        // INSERT INTO cbr.currency(id, frequency, name_rus, name_eng, nominal, parent_code)
        CurrencyDto currency = currencyList.get(0);
        System.out.println("##83: " +
                currency.getId() + ", " +
                currency.getFrequency() + ", " +
                currency.getNameRus() +  ", " +
                currency.getNameEng() +  ", " +
                currency.getNominal() +  ", " +
                currency.getParentCode());

        // Выполнение операций с базой данных
        /*int count = jdbcTemplate.queryForObject("SELECT count(*) FROM cbr.currency", Integer.class);
        assertThat(count).isEqualTo(currencyList.size());

        // Сравнение данных с ожидаемыми, исключая определенные поля
        IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/sdrl-expected-datasets/currency-list.xml"));
        ITable expectedTable = expectedDataSet.getTable("currency");

        // Используем QueryDataSet для получения данных из таблицы
        QueryDataSet actualDataSet = new QueryDataSet(databaseTester.getConnection());
        actualDataSet.addTable("CURRENCY", "SELECT * FROM CBR.CURRENCY");

        ITable actualTable = actualDataSet.getTable("CURRENCY");

        // Исключаем поля времени
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(expectedTable, new String[]{"creation_time", "update_time"});
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(actualTable, new String[]{"creation_time", "update_time"});

        Assertion.assertEquals(filteredExpectedTable, filteredActualTable);*/
    }

    // HSQLDB doesn't support
    private void loadCurrency(List<CurrencyDto> currencyList) {
        try {
            for (CurrencyDto currency : currencyList) {
                jdbcTemplate.update("MERGE INTO cbr.currency AS target " +
                                "USING (VALUES (?, ?, ?, ?, ?, ?)) AS source (id, frequency, name_rus, name_eng, nominal, parent_code) " +
                                "ON target.id = source.id " +
                                "WHEN MATCHED THEN UPDATE SET " +
                                "target.frequency = source.frequency, " +
                                "target.name_rus = source.name_rus, " +
                                "target.name_eng = source.name_eng, " +
                                "target.nominal = source.nominal, " +
                                "target.parent_code = source.parent_code, " +
                                "target.update_time = NOW() " +
                                "WHEN NOT MATCHED THEN INSERT (id, frequency, name_rus, name_eng, nominal, parent_code) " +
                                "VALUES (source.id, source.frequency, source.name_rus, source.name_eng, source.nominal, source.parent_code);",
                        currency.getId(), currency.getFrequency(), currency.getNameRus(), currency.getNameEng(),
                        currency.getNominal(), currency.getParentCode());
            }
        } catch (Exception e) {
            System.out.println("Error (loadCurrency). Message --> " + e.getMessage());
        }
    }
}