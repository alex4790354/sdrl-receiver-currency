package com.github.alex4790354.service;

import org.dbunit.Assertion;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
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
        // Настройка DBUnit
        databaseTester = new JdbcDatabaseTester(
                "org.hsqldb.jdbc.JDBCDriver",
                //"jdbc:hsqldb:mem:sdrldb",
                "jdbc:hsqldb:file:src/test/resources/testdb/sdrldb;schema=CBR",
                "sa",
                ""
        );

        IDataSet dataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/sdrl-expected-datasets/currency-list.xml"));
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.onSetup();
    }

    @Test
    //@Disabled
    public void testDatabaseOperation() throws Exception {
        // Выполнение операций с базой данных
        int count = jdbcTemplate.queryForObject("SELECT 6", Integer.class);
        assertThat(count).isEqualTo(69);

        // Сравнение данных с ожидаемыми, исключая определенные поля
        /*IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new FileInputStream("src/test/resources/sdrl-expected-datasets/currency-list.xml"));
        ITable expectedTable = expectedDataSet.getTable("currency");

        IDataSet actualDataSet = databaseTester.getConnection().createDataSet();
        ITable actualTable = actualDataSet.getTable("currency");

        // Исключаем поля времени
        ITable filteredExpectedTable = DefaultColumnFilter.excludedColumnsTable(expectedTable, new String[]{"creation_time", "update_time"});
        ITable filteredActualTable = DefaultColumnFilter.excludedColumnsTable(actualTable, new String[]{"creation_time", "update_time"});

        Assertion.assertEquals(filteredExpectedTable, filteredActualTable);*/
    }
}