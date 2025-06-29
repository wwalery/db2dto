package dev.walgo.db2dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import dev.walgo.db2dto.config.Config;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class ProcessorTest_HSQL {

//    private static final Logger LOG = LoggerFactory.getLogger(ProcessorTest.class);

    private static final String DB_USER = "sa";
    private static final String DB_URL = "jdbc:hsqldb:mem:testdb";
    private static final String TABLE_2 = "test_table_2";
    private static final String TABLE_1 = "test_table_1";
    private static final String FIELD_OBJECT = "test_object";
    private static final String FIELD_ARRAY = "test_array";
//    private static final String FIELD_ENUM = "enum_field";
    private static final String FIELD_ENUM_2 = "enum_field_2";
    private static final String FIELD_INT = "read_only";
    private static final String DECIMAL_FIELD_1 = "decimal_field_1";
    private static final String DECIMAL_FIELD_2 = "decimal_field_2";
    private static final String FIELD_ADD_1 = "add_field";
    private static final String FIELD_ADD_2 = "add_field_2";
    private static final String FIELD_ADD_3 = "add_field_3";
    private static final String FIELD_ADD_4 = "add_field_4";
    private static final String FIELD_ADD_5 = "add_field_5";

    private static final String TYPE_INTEGER = "Integer";

    private static Connection conn;
    private static Config config;

    @BeforeAll
    public static void before() throws SQLException, IOException, SqlToolError {
        conn = DriverManager.getConnection(DB_URL, DB_USER, "");
        try (InputStream inputStream = new FileInputStream("src/test/resources/db/create_db_hsql.sql")) {
            SqlFile sqlFile = new SqlFile(
                    new InputStreamReader(inputStream),
                    "init",
                    System.out,
                    "UTF-8",
                    false,
                    new File("."));
            sqlFile.setConnection(conn);
            sqlFile.execute();
        }
    }

    @AfterAll
    public static void after() throws SQLException {
        conn.close();
        // Files.delete(Paths.get("testdb.log"));
        // Files.delete(Paths.get("testdb.properties"));
        // Files.delete(Paths.get("testdb.script"));
        // Files.delete(Paths.get("testdb.tmp"));
    }

    /**
     * Test of execute method, of class Processor.
     *
     */
    @Test
    public void testExecute() throws Exception {
        String configStr = Files.readString(Paths.get("./examples/db2dto.conf"));
        Gson gson = new Gson();
        config = gson.fromJson(configStr, Config.class);
        config.dbURL = DB_URL;
        config.dbUser = DB_USER;
        config.dbSchema = "PUBLIC";
        Processor instance = new Processor();
        instance.setConfig(config);
        instance.execute();
        TestUtil.config = config;

        Map<String, DBTable> tables = instance.getTables();
        TestUtil.checkField(tables, TABLE_2, FIELD_OBJECT, TestClass.class.getName());
        TestUtil.checkField(tables, TABLE_2, FIELD_ARRAY, TestType.class.getName());
        TestUtil.checkField(tables, TABLE_2, FIELD_ENUM_2, "String");
        TestUtil.checkField(tables, TABLE_2, FIELD_INT, TYPE_INTEGER);

        // test original field name
        DBColumn field = TestUtil.findField(tables, TABLE_1, DECIMAL_FIELD_1);
        assertThat(field.javaFieldName).isEqualTo("decimalField1");
        assertThat(field.javaPropertyName).isEqualTo("DecimalField1");

        // test renamed field
        field = TestUtil.findField(tables, TABLE_1, DECIMAL_FIELD_2);
        assertThat(field.javaFieldName).isEqualTo("extraField");
        assertThat(field.javaPropertyName).isEqualTo("ExtraField");

        // test additional type field
        field = TestUtil.findField(tables, TABLE_1, FIELD_ADD_1);
        assertThat(field.isSimpleType).isFalse();
        assertThat(field.isNullable).isTrue();
        assertThat(field.javaType).isEqualTo(TYPE_INTEGER);

        // test simple type field
        field = TestUtil.findField(tables, TABLE_1, FIELD_ADD_2);
        assertThat(field.isSimpleType).isTrue();
        assertThat(field.isNullable).isFalse();
        assertThat(field.javaType).isEqualTo("int");

        // test collections type field
        field = TestUtil.findField(tables, TABLE_1, FIELD_ADD_3);
        assertThat(field.javaType).isEqualTo("Map<String, String>");
        field = TestUtil.findField(tables, TABLE_1, FIELD_ADD_4);
        assertThat(field.javaType).isEqualTo("Set<String>");
        field = TestUtil.findField(tables, TABLE_1, FIELD_ADD_5);
        assertThat(field.javaType).isEqualTo("List<String>");
    }
}
