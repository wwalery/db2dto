package dev.walgo.db2dto;

import static org.assertj.core.api.Assertions.assertThat;

import dev.walgo.db2dto.config.Config;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestUtil {

    static Config config;

    private TestUtil() {
        // empty
    }

    static DBColumn findField(
            Map<String, DBTable> tables, final String tableName, final String fieldName) {
        assertThat(tables).containsKey(tableName);
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

    static DBColumn checkField(
            Map<String, DBTable> tables, String table, String fieldName, String fieldType) {
        DBColumn field = findField(tables, table, fieldName);
        assertThat(field.javaType).isEqualTo(fieldType);
        return field;
    }

}
