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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** @author Walery Wysotsky <dev@wysotsky.info> */
public class ProcessorTest {

//    private static final Logger LOG = LoggerFactory.getLogger(ProcessorTest.class);

    private static final String DB_USER = "sa";
    private static final String DB_URL = "jdbc:hsqldb:mem:testdb";
    private static final String TABLE_2 = "test_table_2";
    private static final String TABLE_1 = "test_table_1";
    private static final String FIELD_OBJECT = "test_object";
    private static final String FIELD_ARRAY = "test_array";
    private static final String FIELD_ENUM = "enum_field";
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

    private Connection conn;
    private Config config;

    @Before
    public void before() throws SQLException, IOException, SqlToolError {
        conn = DriverManager.getConnection(DB_URL, DB_USER, "");
        try (InputStream inputStream = new FileInputStream("src/test/resources/db/create_db.sql")) {
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

    @After
    public void after() throws SQLException, IOException {
        conn.close();
        // Files.delete(Paths.get("testdb.log"));
        // Files.delete(Paths.get("testdb.properties"));
        // Files.delete(Paths.get("testdb.script"));
        // Files.delete(Paths.get("testdb.tmp"));
    }

    private DBColumn findField(
            Map<String, DBTable> tables, final String tableName, final String fieldName) {
        Assert.assertTrue(tables.containsKey(tableName));
        DBTable dbTable = tables.get(tableName);
        List<DBColumn> columns = dbTable.columns;
        Optional<DBColumn> field = columns.stream().filter(it -> fieldName.equals(it.name)).findFirst();
        if (field.isEmpty()) {
            columns = config.getFields(tableName);
            field = columns.stream().filter(it -> fieldName.equals(it.name)).findFirst();
        }
        assertThat(field.isPresent())
                .as("Field not found: table: [%s], field: [%s]".formatted(tableName, fieldName))
                .isTrue();
        return field.get();
    }

    private void checkField(
            Map<String, DBTable> tables, String table, String fieldName, String fieldType) {
        DBColumn field = findField(tables, table, fieldName);
        Assert.assertEquals(fieldType, field.javaType);
    }

    /**
     * Test of execute method, of class Processor.
     *
     * @throws java.lang.Exception
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

        Map<String, DBTable> tables = instance.getTables();
        checkField(tables, TABLE_2, FIELD_OBJECT, TestClass.class.getName());
        checkField(tables, TABLE_2, FIELD_ARRAY, TestType.class.getName());
        checkField(tables, TABLE_2, FIELD_ENUM_2, "String");
        checkField(tables, TABLE_2, FIELD_INT, TYPE_INTEGER);

        // test original field name
        DBColumn field = findField(tables, TABLE_1, DECIMAL_FIELD_1);
        Assert.assertEquals("decimalField1", field.javaFieldName);
        Assert.assertEquals("DecimalField1", field.javaPropertyName);

        // test renamed field
        field = findField(tables, TABLE_1, DECIMAL_FIELD_2);
        Assert.assertEquals("extraField", field.javaFieldName);
        Assert.assertEquals("ExtraField", field.javaPropertyName);

        // test additional type field
        field = findField(tables, TABLE_1, FIELD_ADD_1);
        Assert.assertFalse(field.isSimpleType);
        Assert.assertTrue(field.isNullable);
        Assert.assertEquals(TYPE_INTEGER, field.javaType);

        // test simple type field
        field = findField(tables, TABLE_1, FIELD_ADD_2);
        Assert.assertTrue(field.isSimpleType);
        Assert.assertFalse(field.isNullable);
        Assert.assertEquals("int", field.javaType);

        // test collections type field
        field = findField(tables, TABLE_1, FIELD_ADD_3);
        Assert.assertEquals("Map<String, String>", field.javaType);
        field = findField(tables, TABLE_1, FIELD_ADD_4);
        Assert.assertEquals("Set<String>", field.javaType);
        field = findField(tables, TABLE_1, FIELD_ADD_5);
        Assert.assertEquals("List<String>", field.javaType);
    }
}
