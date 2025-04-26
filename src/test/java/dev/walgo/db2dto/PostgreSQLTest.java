package dev.walgo.db2dto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQLTest {

    protected static final String DB_NAME = "test";
    protected static final String DB_USER = "sa";
    protected static final String DB_PASSWORD = "password";
    protected static final String DB_SQL = "db/create_db_postgres.sql";
//    protected static final String DB_SCHEMA = "public";

    protected static final PostgreSQLContainer<?> dbContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASSWORD)
            .withInitScript(DB_SQL);

    static {
        dbContainer.start();
    }

    protected static Connection conn;

    @BeforeAll
    public static void before() throws SQLException {
        conn = DriverManager.getConnection(dbContainer.getJdbcUrl(), DB_USER, DB_PASSWORD);
    }

    @AfterAll
    public static void after() throws SQLException {
        conn.close();
    }

}
